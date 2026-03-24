package com.wasmo.installedapps

import com.wasmo.api.InstallIncompleteReason
import com.wasmo.computers.AppManifestAddress
import com.wasmo.computers.AppManifestAddress.Companion.toAppManifestAddress
import com.wasmo.db.InstalledApp
import com.wasmo.db.WasmoDb
import com.wasmo.events.EventListener
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.StateUserException
import com.wasmo.framework.checkUser
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Resource
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.FileSystem
import okio.Path
import wasmo.http.HttpRequest
import wasmo.http.HttpService
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest
import wasmo.objectstore.ScopedObjectStore

@Inject
@SingleIn(InstalledAppScope::class)
class AppInstaller(
  private val computerSlug: ComputerSlug,
  private val installedApp: InstalledApp,
  private val clock: Clock,
  private val httpService: HttpService,
  @ForInstalledApp private val installedAppObjectStore: ObjectStore,
  private val fileSystem: FileSystem,
  private val eventListener: EventListener,
  private val wasmoDb: WasmoDb,
  private val manifest: AppManifest,
) {
  val resourcesObjectStore: ScopedObjectStore = ScopedObjectStore(
    delegate = installedAppObjectStore,
    prefix = "resources/v${manifest.version}/",
  )

  suspend fun install() {
    val appManifestAddress = installedApp.manifest_address.toAppManifestAddress()
    val installResult = when (appManifestAddress) {
      is AppManifestAddress.Http -> install(appManifestAddress)
      is AppManifestAddress.FileSystem -> install(appManifestAddress)
    }

    wasmoDb.transaction(noEnclosing = true) {
      if (installResult is ResourceResult.Failed) {
        val rowCount = wasmoDb.installedAppQueries.updateInstalledAppSetInstallIncompleteReason(
          id = installedApp.id,
          expected_version = installedApp.version,
          new_version = installedApp.version + 1L,
          install_incomplete_reason = installResult.reason.name,
        ).value
        require(rowCount == 1L)
      } else {
        val rowCount = wasmoDb.installedAppQueries.updateInstalledAppSetInstallCompletedAt(
          id = installedApp.id,
          expected_version = installedApp.version,
          new_version = installedApp.version + 1L,
          install_completed_at = clock.now(),
        ).value
        require(rowCount == 1L)
      }
    }

    eventListener.onEvent(
      InstallAppEvent(
        appSlug = installedApp.slug,
        computerSlug = computerSlug,
        exception = (installResult as? ResourceResult.Failed)?.exception,
      ),
    )
  }

  private suspend fun install(
    appManifestAddress: AppManifestAddress.Http,
  ): ResourceResult {
    val baseUrl = manifest.base_url?.toHttpUrlOrNull() ?: appManifestAddress.url
    val firstFailure = MutableStateFlow<ResourceResult.Failed?>(null)
    supervisorScope {
      try {
        coroutineScope {
          for (resource in manifest.resource) {
            async {
              val resourceResult = installResource(baseUrl, resource, resourcesObjectStore)
              if (resourceResult is ResourceResult.Failed) {
                firstFailure.compareAndSet(null, resourceResult)
                this@coroutineScope.cancel() // Cancel all other calls to installResource().
              }
            }
          }
        }
      } catch (_: CancellationException) {
      }
    }
    return firstFailure.value ?: ResourceResult.Success
  }

  private suspend fun installResource(
    baseUrl: HttpUrl,
    resource: Resource,
    resourcesObjectStore: ScopedObjectStore,
  ): ResourceResult {
    val loadedResource = try {
      loadResource(baseUrl, resource)
    } catch (e: Exception) {
      return ResourceResult.Failed(
        reason = InstallIncompleteReason.SourceUnavailable,
        exception = e,
      )
    }

    // TODO: handle resource.unzip

    try {
      resourcesObjectStore.put(
        PutObjectRequest(
          key = loadedResource.path,
          value = loadedResource.bytes,
          contentType = resource.content_type ?: loadedResource.contentType,
        ),
      )
    } catch (e: Exception) {
      return ResourceResult.Failed(
        reason = InstallIncompleteReason.TargetCapacity,
        exception = e,
      )
    }

    return ResourceResult.Success
  }

  private suspend fun loadResource(
    baseUrl: HttpUrl,
    resource: Resource,
  ): LoadedResource {
    val resourcePath = resource.bestResourcePath()
    val downloadUrl = resource.url.toHttpUrlOrNull()
      ?: baseUrl.resolve(resource.url)
      ?: throw StateUserException("unexpected resource URL: '${resource.url}'")

    val response = httpService.execute(
      HttpRequest(
        method = "GET",
        url = downloadUrl,
      ),
    )

    checkUser(response.isSuccessful) {
      "failed to fetch $downloadUrl: HTTP ${response.code}"
    }

    val expectedSha256 = resource.sha256?.decodeHex()
    checkUser(expectedSha256 == null || expectedSha256 == response.body.sha256()) {
      "response body data for $resourcePath didn't match sha256 from manifest"
    }

    return LoadedResource(
      path = resourcePath,
      bytes = response.body,
      contentType = response.contentType,
    )
  }

  private suspend fun install(
    appManifestAddress: AppManifestAddress.FileSystem,
  ): ResourceResult {
    return withContext(Dispatchers.IO) {
      for (resource in manifest.resource) {
        val resourceResult = checkResource(
          basePath = appManifestAddress.basePath,
          resource = resource,
        )
        if (resourceResult is ResourceResult.Failed) {
          return@withContext resourceResult
        }
      }
      return@withContext ResourceResult.Success
    }
  }

  private fun checkResource(
    basePath: Path,
    resource: Resource,
  ): ResourceResult {
    return try {
      fileSystem.read(basePath.resolve(resource.url)) {
        return ResourceResult.Success
      }
    } catch (e: Exception) {
      ResourceResult.Failed(
        reason = InstallIncompleteReason.SourceUnavailable,
        exception = e,
      )
    }
  }

  private class LoadedResource(
    val path: String,
    val bytes: ByteString,
    val contentType: String?,
  ) {
    init {
      require(!path.startsWith("/"))
    }
  }

  /** Computes a destination path for this resource, computing it from the URL if necessary. */
  private fun Resource.bestResourcePath(): String {
    val resourcePathPrefixed = resource_path
      ?: "https://example.com/".toHttpUrl().resolve(url)?.encodedPath
      ?: throw StateUserException("no resource_path: $this")
    return resourcePathPrefixed.removePrefix("/")
  }

  internal sealed interface ResourceResult {
    object Success : ResourceResult

    class Failed(
      val reason: InstallIncompleteReason,
      val exception: Throwable,
    ) : ResourceResult
  }
}

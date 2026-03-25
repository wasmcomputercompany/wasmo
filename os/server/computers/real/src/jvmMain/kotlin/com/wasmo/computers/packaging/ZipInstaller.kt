package com.wasmo.computers.packaging


//package com.wasmo.computers
//
//
//class RealManifestLoader(
//  private val fileSystem: FileSystem,
//  private val httpService: HttpService,
//) : ManifestLoader {
//  override suspend fun load(
//    appManifestAddress: AppManifestAddress,
//  ): AppManifest {
//    val appManifestString = when (appManifestAddress) {
//      is AppManifestAddress.Http -> {
//        val manifestResponse = try {
//          httpService.execute(
//            HttpRequest(
//              method = "GET",
//              url = appManifestAddress.url,
//            ),
//          )
//        } catch (e: IOException) {
//          throw StateUserException("failed to fetch manifest", e)
//        }
//
//        checkUser(manifestResponse.isSuccessful) {
//          "failed to fetch manifest: HTTP ${manifestResponse.code}"
//        }
//
//        manifestResponse.body.utf8()
//      }
//
//      is AppManifestAddress.FileSystem -> {
//        fileSystem.read(appManifestAddress.path) {
//          readUtf8()
//        }
//      }
//    }
//
//    val result = try {
//      WasmoToml.decodeFromString(
//        AppManifest.serializer(),
//        appManifestString,
//      )
//    } catch (e: Throwable) {
//      throw StateUserException("failed to decode manifest\n\n${e.message}")
//    }
//
//    val issues = result.check()
//    checkUser(issues.isEmpty()) {
//      "invalid manifest\n\n${issues.joinToString(separator = "\n\n")}"
//    }
//
//    return result
//  }
//}

//package com.wasmo.installedapps
//
//import com.wasmo.api.InstallIncompleteReason
//import com.wasmo.identifiers.AppManifestAddress
//import com.wasmo.identifiers.AppManifestAddress.Companion.toAppManifestAddress
//import com.wasmo.db.InstalledApp
//import com.wasmo.db.WasmoDb
//import com.wasmo.events.EventListener
//import com.wasmo.events.InstallAppEvent
//import com.wasmo.framework.StateUserException
//import com.wasmo.framework.checkUser
//import com.wasmo.identifiers.ComputerSlug
//import com.wasmo.packaging.AppManifest
//import com.wasmo.packaging.ExternalResource
//import dev.zacsweers.metro.Inject
//import dev.zacsweers.metro.SingleIn
//import kotlin.time.Clock
//import kotlinx.coroutines.CancellationException
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.supervisorScope
//import kotlinx.coroutines.withContext
//import okhttp3.HttpUrl
//import okhttp3.HttpUrl.Companion.toHttpUrl
//import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
//import okio.ByteString
//import okio.ByteString.Companion.decodeHex
//import okio.FileSystem
//import okio.Path
//import wasmo.http.HttpRequest
//import wasmo.http.HttpService
//import wasmo.objectstore.ObjectStore
//import wasmo.objectstore.PutObjectRequest
//import wasmo.objectstore.ScopedObjectStore
//
//@Inject
//@SingleIn(InstalledAppScope::class)
//class AppInstaller(
//  private val computerSlug: ComputerSlug,
//  private val installedApp: InstalledApp,
//  private val clock: Clock,
//  private val httpService: HttpService,
//  @ForInstalledApp private val installedAppObjectStore: ObjectStore,
//  private val fileSystem: FileSystem,
//  private val eventListener: EventListener,
//  private val wasmoDb: WasmoDb,
//  private val manifest: AppManifest,
//) {
//  val resourcesObjectStore: ScopedObjectStore = ScopedObjectStore(
//    delegate = installedAppObjectStore,
//    prefix = "resources/v${manifest.version}/",
//  )
//
//  suspend fun install() {
//    val appManifestAddress = installedApp.manifest_address.toAppManifestAddress()
//    val installResult = when (appManifestAddress) {
//      is AppManifestAddress.Http -> install(appManifestAddress)
//      is AppManifestAddress.FileSystem -> install(appManifestAddress)
//    }
//
//    wasmoDb.transaction(noEnclosing = true) {
//      if (installResult is ResourceResult.Failed) {
//        val rowCount = wasmoDb.installedAppQueries.updateInstalledAppSetInstallIncompleteReason(
//          id = installedApp.id,
//          expected_version = installedApp.version,
//          new_version = installedApp.version + 1L,
//          install_incomplete_reason = installResult.reason.name,
//        ).value
//        require(rowCount == 1L)
//      } else {
//        val rowCount = wasmoDb.installedAppQueries.updateInstalledAppSetInstallCompletedAt(
//          id = installedApp.id,
//          expected_version = installedApp.version,
//          new_version = installedApp.version + 1L,
//          install_completed_at = clock.now(),
//        ).value
//        require(rowCount == 1L)
//      }
//    }
//
//    eventListener.onEvent(
//      InstallAppEvent(
//        appSlug = installedApp.slug,
//        computerSlug = computerSlug,
//        exception = (installResult as? ResourceResult.Failed)?.exception,
//      ),
//    )
//  }
//
//  private suspend fun install(
//    appManifestAddress: AppManifestAddress.Http,
//  ): ResourceResult {
//    val baseUrl = manifest.base_url?.toHttpUrlOrNull() ?: appManifestAddress.url
//    val firstFailure = MutableStateFlow<ResourceResult.Failed?>(null)
//    supervisorScope {
//      try {
//        coroutineScope {
//          for (resource in manifest.resource) {
//            async {
//              val resourceResult = installResource(baseUrl, resource, resourcesObjectStore)
//              if (resourceResult is ResourceResult.Failed) {
//                firstFailure.compareAndSet(null, resourceResult)
//                this@coroutineScope.cancel() // Cancel all other calls to installResource().
//              }
//            }
//          }
//        }
//      } catch (_: CancellationException) {
//      }
//    }
//    return firstFailure.value ?: ResourceResult.Success
//  }
//
//  private suspend fun installResource(
//    baseUrl: HttpUrl,
//    externalResource: ExternalResource,
//    resourcesObjectStore: ScopedObjectStore,
//  ): ResourceResult {
//    val loadedResource = try {
//      loadResource(baseUrl, externalResource)
//    } catch (e: Exception) {
//      return ResourceResult.Failed(
//        reason = InstallIncompleteReason.SourceUnavailable,
//        exception = e,
//      )
//    }
//
//    // TODO: handle resource.unzip
//
//    try {
//      resourcesObjectStore.put(
//        PutObjectRequest(
//          key = loadedResource.path,
//          value = loadedResource.bytes,
//          contentType = externalResource.content_type ?: loadedResource.contentType,
//        ),
//      )
//    } catch (e: Exception) {
//      return ResourceResult.Failed(
//        reason = InstallIncompleteReason.TargetCapacity,
//        exception = e,
//      )
//    }
//
//    return ResourceResult.Success
//  }
//
//  private suspend fun loadResource(
//    baseUrl: HttpUrl,
//    externalResource: ExternalResource,
//  ): LoadedResource {
//    val resourcePath = externalResource.bestResourcePath()
//    val downloadUrl = externalResource.url.toHttpUrlOrNull()
//      ?: baseUrl.resolve(externalResource.url)
//      ?: throw StateUserException("unexpected resource URL: '${externalResource.url}'")
//
//    val response = httpService.execute(
//      HttpRequest(
//        method = "GET",
//        url = downloadUrl,
//      ),
//    )
//
//    checkUser(response.isSuccessful) {
//      "failed to fetch $downloadUrl: HTTP ${response.code}"
//    }
//
//    val expectedSha256 = externalResource.sha256?.decodeHex()
//    checkUser(expectedSha256 == null || expectedSha256 == response.body.sha256()) {
//      "response body data for $resourcePath didn't match sha256 from manifest"
//    }
//
//    return LoadedResource(
//      path = resourcePath,
//      bytes = response.body,
//      contentType = response.contentType,
//    )
//  }
//
//  private suspend fun install(
//    appManifestAddress: AppManifestAddress.FileSystem,
//  ): ResourceResult {
//    return withContext(Dispatchers.IO) {
//      for (resource in manifest.external_resource) {
//        val resourceResult = checkResource(
//          basePath = appManifestAddress.basePath,
//          externalResource = resource,
//        )
//        if (resourceResult is ResourceResult.Failed) {
//          return@withContext resourceResult
//        }
//      }
//      return@withContext ResourceResult.Success
//    }
//  }
//
//  private fun checkResource(
//    basePath: Path,
//    externalResource: ExternalResource,
//  ): ResourceResult {
//    return try {
//      fileSystem.read(basePath.resolve(externalResource.url)) {
//        return ResourceResult.Success
//      }
//    } catch (e: Exception) {
//      ResourceResult.Failed(
//        reason = InstallIncompleteReason.SourceUnavailable,
//        exception = e,
//      )
//    }
//  }
//
//  private class LoadedResource(
//    val path: String,
//    val bytes: ByteString,
//    val contentType: String?,
//  ) {
//    init {
//      require(!path.startsWith("/"))
//    }
//  }
//
//  /** Computes a destination path for this resource, computing it from the URL if necessary. */
//  private fun ExternalResource.bestResourcePath(): String {
//    val resourcePathPrefixed = resource_path
//      ?: "https://example.com/".toHttpUrl().resolve(url)?.encodedPath
//      ?: throw StateUserException("no resource_path: $this")
//    return resourcePathPrefixed.removePrefix("/")
//  }
//
//  internal sealed interface ResourceResult {
//    object Success : ResourceResult
//
//    class Failed(
//      val reason: InstallIncompleteReason,
//      val exception: Throwable,
//    ) : ResourceResult
//  }
//}

/*
    val installedAppId = wasmoDb.installedAppQueries.insertInstalledApp(
      computer_id = id,
      slug = AppSlug(appManifest.slug),
      manifest_address = appManifestAddress.toString(),
      manifest_data = appManifest,
      version = appManifest.version,
      install_scheduled_at = clock.now(),
    ).executeAsOne()


 */
internal class ZipInstaller : Installer {
  override suspend fun install(): InstallResult {
    TODO("Not yet implemented")
  }
}

package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.InstallIncompleteReason
import com.wasmo.db.AppInstall
import com.wasmo.db.WasmoDb
import com.wasmo.events.AppInstallEvent
import com.wasmo.events.EventListener
import com.wasmo.framework.StateUserException
import com.wasmo.framework.checkUser
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.jobs.JobQueue
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Resource
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.supervisorScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import wasmo.http.HttpClient
import wasmo.http.HttpRequest
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest
import wasmo.objectstore.ScopedObjectStore

/**
 * Save all resources listed in the manifest to the object store.
 */
@Inject
@SingleIn(ComputerScope::class)
class RealAppInstaller(
  private val id: ComputerId,
  private val clock: Clock,
  private val httpClient: HttpClient,
  private val computerSlug: ComputerSlug,
  @ForComputer private val computerObjectStore: ObjectStore,
  private val eventListener: EventListener,
  private val installAppJobQueue: JobQueue<InstallAppJob>,
  private val manifestLoader: ManifestLoader,
  private val wasmoDb: WasmoDb,
) : AppInstaller {
  context(transactionCallbacks: TransactionCallbacks)
  override fun enqueueInstall(
    manifestUrl: HttpUrl,
    manifest: AppManifest,
  ) {
    val appInstallId = wasmoDb.appInstallQueries.insertAppInstall(
      computer_id = id,
      slug = AppSlug(manifest.slug),
      manifest_url = manifestUrl.toString(),
      manifest_data = manifest,
      version = manifest.version,
      install_scheduled_at = clock.now(),
    ).executeAsOne()

    installAppJobQueue.enqueue(InstallAppJob(appInstallId))
  }

  override suspend fun install(appInstall: AppInstall) {
    val manifestUrl = appInstall.manifest_url.toHttpUrl()
    val manifest = manifestLoader.loadManifest(manifestUrl)

    val baseUrl = manifest.base_url?.toHttpUrlOrNull() ?: manifestUrl

    val resourcesObjectStore = ScopedObjectStore(
      delegate = computerObjectStore,
      prefix = "${manifest.slug}/resources/v${manifest.version}/",
    )

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

    val firstFailureValue = firstFailure.value

    wasmoDb.transaction(noEnclosing = true) {
      if (firstFailureValue != null) {
        val rowCount = wasmoDb.appInstallQueries.updateAppInstallSetInstallIncompleteReason(
          id = appInstall.id,
          expected_version = appInstall.version,
          new_version = appInstall.version + 1L,
          install_incomplete_reason = firstFailureValue.reason.name,
        ).value
        require(rowCount == 1L)
      } else {
        val rowCount = wasmoDb.appInstallQueries.updateAppInstallSetInstallCompletedAt(
          id = appInstall.id,
          expected_version = appInstall.version,
          new_version = appInstall.version + 1L,
          install_completed_at = clock.now(),
        ).value
        require(rowCount == 1L)
      }
    }

    eventListener.onEvent(
      AppInstallEvent(
        appSlug = appInstall.slug,
        computerSlug = computerSlug,
        exception = firstFailureValue?.exception,
      ),
    )
  }

  internal suspend fun installResource(
    baseUrl: HttpUrl,
    resource: Resource,
    resourcesObjectStore: ScopedObjectStore,
  ): ResourceResult {
    val resourcePath: String
    val resourceBytes: ByteString
    try {
      val downloadUrl = resource.url.toHttpUrlOrNull()
        ?: baseUrl.resolve(resource.url)
        ?: throw StateUserException("unexpected resource URL: '${resource.url}'")

      resourcePath = resource.resource_path
        ?: "https://example.com/".toHttpUrl().resolve(resource.url)?.encodedPath
          ?: throw StateUserException("no resource_path: $this")

      val response = httpClient.execute(
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
        "response body data for $downloadUrl didn't match sha256 from manifest"
      }

      resourceBytes = response.body
    } catch (e: Exception) {
      return ResourceResult.Failed(
        reason = InstallIncompleteReason.SourceUnavailable,
        exception = e,
      )
    }

    // TODO: handle resource.content_type
    // TODO: handle resource.unzip

    try {
      resourcesObjectStore.put(
        PutObjectRequest(
          key = resourcePath.removePrefix("/"),
          value = resourceBytes,
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

  internal sealed interface ResourceResult {
    object Success : ResourceResult

    class Failed(
      val reason: InstallIncompleteReason,
      val exception: Throwable,
    ) : ResourceResult
  }
}

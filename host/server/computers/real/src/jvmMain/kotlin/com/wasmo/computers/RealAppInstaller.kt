package com.wasmo.computers

import com.wasmo.db.WasmoDb
import com.wasmo.events.AppInstallEvent
import com.wasmo.events.EventListener
import com.wasmo.framework.StateUserException
import com.wasmo.framework.checkUser
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.jobs.JobQueue
import com.wasmo.packaging.Resource
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
  override suspend fun enqueueInstall(manifestUrl: HttpUrl) {
    val manifest = manifestLoader.loadManifest(
      manifestUrl = manifestUrl,
    )

    wasmoDb.transaction(noEnclosing = true) {
      val appInstallId = wasmoDb.appInstallQueries.insertAppInstall(
        computer_id = id,
        slug = AppSlug(manifest.slug),
        manifest_url = manifestUrl.toString(),
        launcher_label = manifest.launcher?.label,
        version = manifest.version,
        install_scheduled_at = clock.now(),
      ).executeAsOne()

      installAppJobQueue.enqueue(InstallAppJob(appInstallId))
    }
  }

  override suspend fun install(
    manifestUrl: HttpUrl,
    appSlug: AppSlug,
  ) {
    val result = runCatching {
      val manifest = manifestLoader.loadManifest(manifestUrl)

      val baseUrl = manifest.base_url?.toHttpUrlOrNull() ?: manifestUrl

      val resourcesObjectStore = ScopedObjectStore(
        delegate = computerObjectStore,
        prefix = "${manifest.slug}/resources/v${manifest.version}/",
      )

      coroutineScope {
        manifest.resource.map {
          async {
            installResource(baseUrl, it, resourcesObjectStore)
          }
        }.awaitAll()
      }
    }

    eventListener.onEvent(
      AppInstallEvent(
        appSlug = appSlug,
        computerSlug = computerSlug,
        exception = result.exceptionOrNull(),
      ),
    )
  }

  suspend fun installResource(
    baseUrl: HttpUrl,
    resource: Resource,
    resourcesObjectStore: ScopedObjectStore,
  ) {
    val downloadUrl = resource.url.toHttpUrlOrNull()
      ?: baseUrl.resolve(resource.url)
      ?: throw StateUserException("unexpected resource URL: '${resource.url}'")

    val resourcePath = resource.resource_path
      ?: "https://example.com/".toHttpUrl().resolve(resource.url)?.encodedPath
      ?: throw StateUserException("no resource_path: $this")

    val response = httpClient.execute(
      HttpRequest(
        method = "GET",
        url = downloadUrl,
      ),
    )

    check(response.isSuccessful) { "unexpected response: $response" }

    val expectedSha256 = resource.sha256?.decodeHex()
    checkUser(expectedSha256 == null || expectedSha256 == response.body.sha256()) {
      "response body data didn't match sha256 from manifest"
    }

    // TODO: handle resource.content_type
    // TODO: handle resource.unzip

    resourcesObjectStore.put(
      PutObjectRequest(
        key = resourcePath.removePrefix("/"),
        value = response.body,
      ),
    )
  }
}

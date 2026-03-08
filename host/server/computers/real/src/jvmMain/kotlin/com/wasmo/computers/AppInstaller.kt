package com.wasmo.computers

import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Resource
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
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
class AppInstaller(
  private val httpClient: HttpClient,
  @ForComputer private val computerObjectStore: ObjectStore,
) {
  suspend fun install(manifestUrl: HttpUrl, manifest: AppManifest) {
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

  suspend fun installResource(
    baseUrl: HttpUrl,
    resource: Resource,
    resourcesObjectStore: ScopedObjectStore,
  ) {
    val downloadUrl = resource.url.toHttpUrlOrNull()
      ?: baseUrl.resolve(resource.url)
      ?: throw IllegalStateException("unexpected resource URL: '${resource.url}'")

    val resourcePath = resource.resource_path
      ?: "https://example.com/".toHttpUrl().resolve(resource.url)?.encodedPath
      ?: throw IllegalArgumentException("no resource_path: $this")

    val response = httpClient.execute(
      HttpRequest(
        method = "GET",
        url = downloadUrl,
      )
    )

    check(response.isSuccessful) { "unexpected response: $response" }

    val expectedSha256 = resource.sha256?.decodeHex()
    check(expectedSha256 == null || expectedSha256 == response.body.sha256()) {
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

package com.wasmo.testing.apps

import com.wasmo.computers.AppCatalog
import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.computers.AppManifestAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import okhttp3.HttpUrl
import okio.ByteString
import wasmo.app.WasmoApp
import wasmo.http.FakeHttpService
import wasmo.http.HttpResponse

/**
 * An installable app, not installed on a particular computer.
 */
data class PublishedApp(
  val appManifestAddress: AppManifestAddress,
  val manifest: AppManifest,
  val resources: Map<String, ByteString>,
  val factory: WasmoApp.Factory,
) {
  val wasm: ByteString?
    get() = resources["app.wasm"]

  val appManifestUrl: HttpUrl? = (appManifestAddress as? AppManifestAddress.Http)?.url

  private val resourcesByUrl = buildMap {
    val manifestUrl = appManifestUrl ?: return@buildMap
    for ((key, value) in resources) {
      put(manifestUrl.resolve(key), value)
    }
  }

  val httpHandler: FakeHttpService.Handler
    get() = FakeHttpService.Handler { request ->
      val resource = resourcesByUrl[request.url]
      when {
        resource != null -> HttpResponse(
          body = resource,
        )

        request.url == appManifestUrl -> HttpResponse(
          toml = WasmoToml,
          body = manifest,
        )

        else -> null
      }
    }
}

val TestAppCatalog = AppCatalog(
  entries = listOf(MusicApp.PublishedApp, SnakeApp.PublishedApp).map {
    Entry(
      appManifestAddress = it.appManifestAddress,
      manifest = it.manifest,
    )
  },
)

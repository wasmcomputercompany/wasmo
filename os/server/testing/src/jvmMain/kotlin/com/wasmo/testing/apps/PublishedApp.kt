package com.wasmo.testing.apps

import com.wasmo.computers.AppCatalog
import com.wasmo.computers.AppCatalog.Entry
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
  val manifestUrl: HttpUrl,
  val manifest: AppManifest,
  val servedResources: Map<HttpUrl, ByteString>,
  val factory: WasmoApp.Factory,
) {
  val wasm: ByteString?
    get() = servedResources.entries
      .firstOrNull { (key, _) -> key.pathSegments.last() == "app.wasm" }
      ?.value

  val httpHandler: FakeHttpService.Handler
    get() = FakeHttpService.Handler { request ->
      val resource = servedResources[request.url]
      when {
        resource != null -> HttpResponse(
          body = resource,
        )

        request.url == manifestUrl -> HttpResponse(
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
      manifestUrl = it.manifestUrl,
      manifest = it.manifest,
    )
  },
)

package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.packaging.WasmoToml
import com.wasmo.testing.apps.PublishedApp
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse

/**
 * A fake server that serves `wasmo-manifest.json` and `.wasm` files.
 */
@Inject
@SingleIn(AppScope::class)
class WasmoArtifactServer() : FakeHttpClient.Handler {
  private val publishedApps = mutableListOf<PublishedApp>()

  fun publish(app: PublishedApp) {
    publishedApps += app
  }

  override fun handle(request: HttpRequest): HttpResponse? {
    for (app in publishedApps) {
      if (request.url == app.manifestUrl) {
        return HttpResponse(
          toml = WasmoToml,
          body = app.manifest,
        )
      }

      val resource = app.servedResources[request.url]
      if (resource != null) {
        return HttpResponse(
          body = resource
        )
      }
    }

    return null
  }
}

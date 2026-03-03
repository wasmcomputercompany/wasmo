package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.api.AppManifest
import com.wasmo.api.AppSlug
import com.wasmo.api.WasmoJson
import com.wasmo.http.HttpRequest
import com.wasmo.http.HttpResponse
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString

/**
 * A fake server that serves `wasmo-manifest.json` and `.wasm` files.
 */
@Inject
@SingleIn(AppScope::class)
class WasmoArtifactServer() : FakeHttpClient.Handler {
  val apps = mutableListOf<App>()

  override fun handle(request: HttpRequest): HttpResponse? {
    for (app in apps) {
      when (request.url.encodedPath) {
        app.manifestPath -> return HttpResponse(
          json = WasmoJson,
          body = AppManifest(
            canonicalUrl = request.url.resolve(app.manifestPath)!!.toString(),
            version = app.version,
            slug = app.slug,
            displayName = app.displayName,
            wasmUrl = request.url.resolve(app.wasmPath)!!.toString(),
            wasmSha256 = app.wasm.sha256(),
          ),
        )

        app.wasmPath -> return HttpResponse(
          body = app.wasm,
        )
      }
    }

    return null
  }

  data class App(
    val slug: AppSlug,
    val displayName: String,
    val version: Long,
    val wasm: ByteString,
  ) {
    val manifestPath: String
      get() = "/$slug/wasmo-manifest.json"
    val wasmPath: String
      get() = "/$slug/$slug.wasm"
  }
}

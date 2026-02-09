package com.wasmo.admin.server

import com.wasmo.FakeHttpClient
import com.wasmo.HttpRequest
import com.wasmo.HttpResponse
import com.wasmo.admin.api.AppManifest
import kotlinx.serialization.json.Json
import okio.ByteString

/**
 * A fake server that serves `wasmo-manifest.json` and `.wasm` files.
 */
class WasmoArtifactServer(
  val json: Json,
) : FakeHttpClient.Handler {
  val apps = mutableListOf<App>()

  override fun handle(request: HttpRequest): HttpResponse? {
    for (app in apps) {
      when (request.url.encodedPath) {
        app.manifestPath -> return HttpResponse(
          json = json,
          body = AppManifest(
            canonicalUrl = request.url.resolve(app.manifestPath)!!.toString(),
            version = 1L,
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
    val name: String,
    val wasm: ByteString,
  ) {
    val manifestPath: String
      get() = "/$name/wasmo-manifest.json"
    val wasmPath: String
      get() = "/$name/$name.wasm"
  }
}

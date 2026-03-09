package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Resource
import com.wasmo.packaging.TargetSdk1
import com.wasmo.packaging.WasmoToml
import com.wasmo.testing.apps.TestApp
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
  val apps = mutableListOf<TestApp>()

  override fun handle(request: HttpRequest): HttpResponse? {
    for (app in apps) {
      when (request.url.encodedPath) {
        app.manifestPath -> return HttpResponse(
          toml = WasmoToml,
          body = AppManifest(
            version = app.version,
            slug = app.slug.value,
            target = TargetSdk1,
            base_url = app.baseUrl.toString(),
            launcher = Launcher(
              label = app.launcherLabel,
            ),
            resource = listOf(
              Resource(
                url = app.wasmPath,
                resource_path = "/app.wasm",
              ),
            ),
          ),
        )

        app.wasmPath -> return HttpResponse(
          body = app.wasm,
        )
      }
    }

    return null
  }
}

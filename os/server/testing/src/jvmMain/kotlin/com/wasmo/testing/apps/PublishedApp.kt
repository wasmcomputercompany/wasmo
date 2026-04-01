package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.testing.buildZip
import okhttp3.HttpUrl
import okio.ByteString
import wasmo.app.WasmoApp
import wasmo.http.FakeHttpService
import wasmo.http.HttpResponse

/**
 * An installable app, not installed on a particular computer.
 */
data class PublishedApp(
  val wasmoFileAddress: WasmoFileAddress,
  val slug: AppSlug,
  val appManifest: AppManifest,
  val resources: Map<String, ByteString>,
  val factory: WasmoApp.Factory,
) {
  val wasm: ByteString?
    get() = resources["app.wasm"]

  val wasmoFileUrl: HttpUrl? = (wasmoFileAddress as? WasmoFileAddress.Http)?.url

  val httpHandler: FakeHttpService.Handler
    get() = FakeHttpService.Handler { request ->
      when (request.url) {
        wasmoFileUrl -> HttpResponse(
          body = buildZip {
            put(appManifest)
            for ((key, value) in resources) {
              put(key, value)
            }
          },
        )

        else -> null
      }
    }
}

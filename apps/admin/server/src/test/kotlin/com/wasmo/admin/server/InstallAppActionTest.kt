package com.wasmo.admin.server

import com.wasmo.admin.api.AppManifest
import com.wasmo.admin.api.InstallAppRequest
import kotlin.test.Test
import okio.ByteString.Companion.encodeUtf8

class InstallAppActionTest {

  @Test
  fun happyPath() {
    val installAppResponse = InstallAppAction().installApp(
      InstallAppRequest(
        manifest = AppManifest(
          canonicalUrl = "https://example.com/app",
          version = 1L,
          wasmUrl = "hello.wasm",
          wasmSha256 = "hello.wasm".encodeUtf8().sha256(),
        ),
      ),
    )
  }
}

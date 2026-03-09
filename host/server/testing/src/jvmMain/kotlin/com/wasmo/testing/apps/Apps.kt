package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

/**
 * An installable app, not installed on a particular computer.
 */
data class TestApp(
  val slug: AppSlug,
  val launcherLabel: String,
  val version: Long,
  val wasm: ByteString,
) {
  val baseUrl: HttpUrl
    get() = "https://example.com/${slug.value}/v$version/".toHttpUrl()
  val manifestPath: String
    get() = "/${slug.value}/v$version/manifest.toml"
  val wasmPath: String
    get() = "/${slug.value}/v$version/app.wasm"
}

val RecipesApp = TestApp(
  slug = AppSlug("recipes"),
  launcherLabel = "Recipes",
  version = 1L,
  wasm = "I am Wasm data".encodeUtf8(),
)

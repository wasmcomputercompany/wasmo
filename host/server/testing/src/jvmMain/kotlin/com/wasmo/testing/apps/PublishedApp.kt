package com.wasmo.testing.apps

import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Resource
import com.wasmo.packaging.TargetSdk1
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

/**
 * An installable app, not installed on a particular computer.
 */
data class PublishedApp(
  val manifestUrl: HttpUrl,
  val manifest: AppManifest,
  val servedResources: Map<HttpUrl, ByteString>,
) {
  val wasm: ByteString?
    get() = servedResources.entries
      .firstOrNull { (key, _) -> key.pathSegments.last() == "app.wasm" }
      ?.value
}

val RecipesApp = PublishedApp(
  manifestUrl = "https://example.com/recipes/v1/recipes.wasmo.toml".toHttpUrl(),
  manifest = AppManifest(
    version = 1L,
    slug = "recipes",
    target = TargetSdk1,
    base_url = "https://example.com/recipes/v1/",
    launcher = Launcher(
      label = "Recipes",
    ),
    resource = listOf(
      Resource(
        url = "app.wasm",
        resource_path = "/app.wasm",
      ),
    ),
  ),
  servedResources = mapOf(
    "https://example.com/recipes/v1/app.wasm".toHttpUrl() to "I am Wasm data".encodeUtf8(),
  ),
)

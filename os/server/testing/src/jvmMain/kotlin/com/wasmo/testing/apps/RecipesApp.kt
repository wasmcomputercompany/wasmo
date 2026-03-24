package com.wasmo.testing.apps

import com.wasmo.computers.ManifestAddress.Companion.toManifestAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Resource
import com.wasmo.packaging.TargetSdk1
import okio.ByteString.Companion.encodeUtf8
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpService

class RecipesApp(
  val platform: Platform,
) : WasmoApp {
  override val httpService: HttpService?
    get() = null

  override suspend fun afterInstall(oldVersion: Long, newVersion: Long) {
  }

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform) = RecipesApp(platform)
  }

  companion object {
    val Manifest = AppManifest(
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
    )

    val PublishedApp = PublishedApp(
      manifestAddress = "https://example.com/recipes/v1/recipes.wasmo.toml".toManifestAddress(),
      manifest = Manifest,
      resources = mapOf(
        "app.wasm" to "I am Wasm data".encodeUtf8(),
      ),
      factory = Factory(),
    )
  }
}

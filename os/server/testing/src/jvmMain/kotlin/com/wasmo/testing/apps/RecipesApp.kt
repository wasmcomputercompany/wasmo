package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Route
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
      target = TargetSdk1,
      launcher = Launcher(
        label = "Recipes",
      ),
      route = listOf(
        Route(
          path = "/",
          resource_path = "/index.html",
        ),
      ),
    )

    val PublishedApp = PublishedApp(
      wasmoFileAddress = "https://example.com/recipes/v1/recipes.wasmo".toWasmoFileAddress(),
      slug = AppSlug("recipes"),
      appManifest = Manifest,
      resources = mapOf(
        "app.wasm" to "I am Wasm data".encodeUtf8(),
        "index.html" to "Welcome to the recipes app".encodeUtf8(),
      ),
      factory = Factory(),
    )
  }
}

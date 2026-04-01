package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Route
import com.wasmo.packaging.TargetSdk1
import com.wasmo.testing.events.AfterInstallEvent
import com.wasmo.testing.events.TestEventQueue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString.Companion.encodeUtf8
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpService

class RecipesApp(
  val eventQueue: TestEventQueue,
  val platform: Platform,
) : WasmoApp {
  override val httpService: HttpService?
    get() = null

  override suspend fun afterInstall(oldVersion: Long, newVersion: Long) {
    eventQueue.send(
      AfterInstallEvent(
        appSlug = Slug,
        oldVersion = oldVersion,
        newVersion = newVersion,
      ),
    )
  }

  @Inject
  @SingleIn(AppScope::class)
  class Factory(
    val eventQueue: TestEventQueue,
  ) : WasmoApp.Factory {
    val appManifest = AppManifest(
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

    val publishedApp = PublishedApp(
      wasmoFileAddress = "https://example.com/recipes/v1/recipes.wasmo".toWasmoFileAddress(),
      slug = Slug,
      appManifest = appManifest,
      resources = mapOf(
        "app.wasm" to "I am Wasm data".encodeUtf8(),
        "index.html" to "Welcome to the recipes app".encodeUtf8(),
      ),
      factory = this,
    )

    override suspend fun create(platform: Platform) = RecipesApp(
      eventQueue = eventQueue,
      platform = platform,
    )
  }

  companion object {
    val Slug = AppSlug("recipes")
  }
}

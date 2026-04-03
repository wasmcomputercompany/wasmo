package com.wasmo.testing.apps

import com.wasmo.events.EventListener
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.Route
import com.wasmo.packaging.TargetSdk1
import com.wasmo.testing.events.AfterInstallEvent
import com.wasmo.testing.jobs.FakeJobHandler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString.Companion.encodeUtf8
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.jobs.JobHandler

class RecipesApp(
  val eventListener: EventListener,
  val platform: Platform,
) : WasmoApp() {
  override val jobHandlerFactory: JobHandler.Factory
    get() = FakeJobHandler.Factory(Slug, eventListener)

  override suspend fun afterInstall(oldVersion: Long, newVersion: Long) {
    eventListener.onEvent(
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
    val eventListener: EventListener,
  ) : WasmoApp.Factory {
    val appManifest = AppManifest(
      version = 1L,
      target = TargetSdk1,
      launcher = Launcher(
        label = "Recipes",
        home_path = "/home",
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
      eventListener = eventListener,
      platform = platform,
    )
  }

  companion object {
    val Slug = AppSlug("recipes")
  }
}

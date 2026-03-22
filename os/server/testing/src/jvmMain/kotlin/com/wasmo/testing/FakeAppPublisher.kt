package com.wasmo.testing

import com.wasmo.packaging.AppManifest
import com.wasmo.testing.apps.MusicApp
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.SnakeApp
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.FakeHttpService

/**
 * A fake server that serves `wasmo-manifest.json` and `.wasm` files.
 */
@Inject
@SingleIn(AppScope::class)
class FakeAppPublisher : AppLoader {
  private val publishedApps = mutableListOf(
    MusicApp.PublishedApp,
    SnakeApp.PublishedApp,
  )

  val httpHandler: FakeHttpService.Handler
    get() = FakeHttpService.Handler { request ->
      publishedApps.firstNotNullOfOrNull { it.httpHandler.handle(request) }
    }

  fun publish(app: PublishedApp) {
    publishedApps += app
  }

  override suspend fun load(
    platform: Platform,
    manifest: AppManifest,
  ): WasmoApp? {
    val publishedApp = publishedApps.firstOrNull { it.manifest.slug == manifest.slug }
      ?: return null
    return publishedApp.factory.create(platform)
  }
}

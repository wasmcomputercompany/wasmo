package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import com.wasmo.testing.events.AfterInstallEvent
import com.wasmo.testing.events.TestEventQueue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpService

class SnakeApp(
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
      version = 3L,
      target = TargetSdk1,
      launcher = Launcher(
        label = "Snake",
      ),
    )
    val publishedApp = PublishedApp(
      wasmoFileAddress = "https://example.com/snake/v3/snake.wasmo".toWasmoFileAddress(),
      slug = Slug,
      appManifest = appManifest,
      resources = mapOf(),
      factory = this,
    )

    override suspend fun create(platform: Platform) = SnakeApp(
      eventQueue = eventQueue,
      platform = platform,
    )
  }

  companion object {
    val Slug = AppSlug("snake")
  }
}

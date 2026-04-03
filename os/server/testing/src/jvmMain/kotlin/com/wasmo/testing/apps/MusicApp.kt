package com.wasmo.testing.apps

import com.wasmo.events.EventListener
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import com.wasmo.testing.events.AfterInstallEvent
import com.wasmo.testing.jobs.FakeJobHandler
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.jobs.JobHandler

class MusicApp(
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
  @SingleIn(OsScope::class)
  class Factory(
    val eventListener: EventListener,
  ) : WasmoApp.Factory {
    val appManifest = AppManifest(
      version = 2L,
      target = TargetSdk1,
      launcher = Launcher(
        label = "Music",
      ),
    )

    val publishedApp = PublishedApp(
      wasmoFileAddress = "https://example.com/music/v2/music.wasmo".toWasmoFileAddress(),
      slug = Slug,
      appManifest = appManifest,
      resources = mapOf(),
      factory = this,
    )

    override suspend fun create(platform: Platform) = MusicApp(
      eventListener = eventListener,
      platform = platform,
    )
  }

  companion object {
    val Slug = AppSlug("music")
  }
}

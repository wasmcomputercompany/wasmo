package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpService

class MusicApp(
  val platform: Platform,
) : WasmoApp {
  override val httpService: HttpService?
    get() = null

  override suspend fun afterInstall(oldVersion: Long, newVersion: Long) {
  }

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform) = MusicApp(platform)
  }

  companion object {
    val Manifest = AppManifest(
      version = 2L,
      target = TargetSdk1,
      launcher = Launcher(
        label = "Music",
      ),
    )

    val PublishedApp = PublishedApp(
      wasmoFileAddress = "https://example.com/music/v2/music.wasmo".toWasmoFileAddress(),
      slug = AppSlug("music"),
      appManifest = Manifest,
      resources = mapOf(),
      factory = Factory(),
    )
  }
}

package com.wasmo.testing.apps

import com.wasmo.computers.ManifestAddress.Companion.toManifestAddress
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
      slug = "music",
      target = TargetSdk1,
      base_url = "https://example.com/music/v2/",
      launcher = Launcher(
        label = "Music",
      ),
    )

    val PublishedApp = PublishedApp(
      manifestAddress = "https://example.com/music/v2/music.wasmo.toml".toManifestAddress(),
      manifest = Manifest,
      resources = mapOf(),
      factory = Factory(),
    )
  }
}

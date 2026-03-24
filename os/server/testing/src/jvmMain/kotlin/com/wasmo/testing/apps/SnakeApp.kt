package com.wasmo.testing.apps

import com.wasmo.computers.ManifestAddress.Companion.toManifestAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.HttpService

class SnakeApp(
  val platform: Platform,
) : WasmoApp {
  override val httpService: HttpService?
    get() = null

  override suspend fun afterInstall(oldVersion: Long, newVersion: Long) {
  }

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform) = SnakeApp(platform)
  }

  companion object {
    val Manifest = AppManifest(
      version = 3L,
      slug = "snake",
      target = TargetSdk1,
      base_url = "https://example.com/snake/v3/",
      launcher = Launcher(
        label = "Snake",
      ),
    )
    val PublishedApp = PublishedApp(
      manifestAddress = "https://example.com/snake/v3/snake.wasmo.toml".toManifestAddress(),
      manifest = Manifest,
      resources = mapOf(),
      factory = Factory(),
    )
  }
}

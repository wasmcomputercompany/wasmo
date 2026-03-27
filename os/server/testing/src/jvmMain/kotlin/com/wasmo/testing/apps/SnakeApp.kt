package com.wasmo.testing.apps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
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
      target = TargetSdk1,
      launcher = Launcher(
        label = "Snake",
      ),
    )
    val PublishedApp = PublishedApp(
      wasmoFileAddress = "https://example.com/snake/v3/snake.wasmo".toWasmoFileAddress(),
      slug = AppSlug("snake"),
      appManifest = Manifest,
      resources = mapOf(),
      factory = Factory(),
    )
  }
}

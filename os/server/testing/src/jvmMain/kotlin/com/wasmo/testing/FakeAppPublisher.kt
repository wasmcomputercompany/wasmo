package com.wasmo.testing

import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import com.wasmo.testing.apps.MusicApp
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.SnakeApp
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.encodeToString
import okio.FileSystem
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.http.FakeHttpService

/**
 * A fake server that serves `wasmo-manifest.json` and `.wasm` files.
 */
@Inject
@SingleIn(AppScope::class)
class FakeAppPublisher(
  private val fileSystem: FileSystem,
) : AppLoader {
  private val publishedApps = mutableListOf(
    MusicApp.PublishedApp,
    SnakeApp.PublishedApp,
  )

  val httpHandler: FakeHttpService.Handler
    get() = FakeHttpService.Handler { request ->
      publishedApps.firstNotNullOfOrNull { it.httpHandler.handle(request) }
    }

  fun publish(app: PublishedApp) {
    publishedApps.removeAll { it.appManifest.slug == app.appManifest.slug }
    publishedApps += app

    if (app.wasmoFileAddress is WasmoFileAddress.FileSystem) {
      fileSystem.deleteRecursively(app.wasmoFileAddress.path)
      fileSystem.createDirectories(app.wasmoFileAddress.path)

      fileSystem.write(app.wasmoFileAddress.path / "wasmo-manifest.toml") {
        writeUtf8(WasmoToml.encodeToString(app.appManifest))
      }

      for ((key, value) in app.resources) {
        val resourcePath = app.wasmoFileAddress.path / key
        fileSystem.createDirectories(resourcePath.parent!!)
        fileSystem.write(resourcePath) {
          write(value)
        }
      }
    }
  }

  override suspend fun load(
    platform: Platform,
    manifest: AppManifest,
  ): WasmoApp? {
    val publishedApp = publishedApps.firstOrNull { it.appManifest.slug == manifest.slug }
      ?: return null
    return publishedApp.factory.create(platform)
  }
}

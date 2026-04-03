package com.wasmo.testing

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.WasmoToml
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.SampleApps
import com.wasmo.wasm.AppLoader
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
@SingleIn(OsScope::class)
class FakeAppPublisher(
  private val fileSystem: FileSystem,
  private val sampleApps: SampleApps,
) : AppLoader {
  private val publishedApps = mutableListOf(
    sampleApps.music.publishedApp,
    sampleApps.snake.publishedApp,
  )

  val httpHandler: FakeHttpService.Handler
    get() = FakeHttpService.Handler { request ->
      publishedApps.firstNotNullOfOrNull { it.httpHandler.handle(request) }
    }

  fun publish(app: PublishedApp) {
    publishedApps.removeAll { it.slug == app.slug }
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
    appSlug: AppSlug,
  ): WasmoApp? {
    val publishedApp = publishedApps.firstOrNull { it.slug == appSlug }
      ?: return null
    return publishedApp.factory.create(platform)
  }
}

package com.wasmo.testing

import com.wasmo.computers.AppManifestAddress
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
    publishedApps.removeAll { it.manifest.slug == app.manifest.slug }
    publishedApps += app

    if (app.appManifestAddress is AppManifestAddress.FileSystem) {
      val basePath = app.appManifestAddress.basePath
      fileSystem.deleteRecursively(basePath)

      fileSystem.createDirectories(basePath)

      fileSystem.write(app.appManifestAddress.path) {
        writeUtf8(WasmoToml.encodeToString(app.manifest))
      }

      for ((key, value) in app.resources) {
        val resourcePath = basePath / key
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
    val publishedApp = publishedApps.firstOrNull { it.manifest.slug == manifest.slug }
      ?: return null
    return publishedApp.factory.create(platform)
  }
}

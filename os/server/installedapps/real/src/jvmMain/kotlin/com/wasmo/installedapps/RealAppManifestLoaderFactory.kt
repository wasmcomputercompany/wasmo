package com.wasmo.installedapps

import com.wasmo.db.InstalledApp
import com.wasmo.db.InstalledAppRelease
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import dev.eav.tomlkt.decodeFromString
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path

/**
 * Picks a strategy to load a manifest, without actually loading it.
 */
@Inject
@SingleIn(OsScope::class)
class RealAppManifestLoaderFactory(
  private val fileSystem: FileSystem,
) {
  fun create(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): AppManifestLoader {
    val wasmoFileAddress = installedApp.wasmo_file_address
    if (wasmoFileAddress is WasmoFileAddress.FileSystem) {
      return FileSystemAppManifestLoader(fileSystem, wasmoFileAddress.path)
    }

    if (installedAppRelease != null) {
      return ImmediateAppManifestLoader(installedAppRelease.app_manifest_data)
    }

    return EmptyAppManifestLoader
  }
}

private class ImmediateAppManifestLoader(
  private val appManifest: AppManifest,
) : AppManifestLoader {
  override suspend fun load() = appManifest
}

private class FileSystemAppManifestLoader(
  private val fileSystem: FileSystem,
  private val wasmoFilePath: Path,
) : AppManifestLoader {
  private val loadedReference = AtomicReference<AppManifest?>(null)

  override suspend fun load(): AppManifest {
    val loaded = loadedReference.get()
    if (loaded != null) return loaded

    val appManifest = withContext(Dispatchers.IO) {
      try {
        fileSystem.read(wasmoFilePath / "wasmo-manifest.toml") {
          WasmoToml.decodeFromString<AppManifest>(readUtf8())
        }
      } catch (_: FileNotFoundException) {
        PlaceholderManifest
      } catch (_: SerializationException) {
        PlaceholderManifest
      }
    }

    loadedReference.compareAndSet(null, appManifest)
    return appManifest
  }
}

/** This is used while waiting for an app to install from an HTTP service. */
private object EmptyAppManifestLoader : AppManifestLoader {
  override suspend fun load() = PlaceholderManifest
}

private val PlaceholderManifest = AppManifest(
  target = "https://wasmo.com/sdk/1",
  version = 0,
)

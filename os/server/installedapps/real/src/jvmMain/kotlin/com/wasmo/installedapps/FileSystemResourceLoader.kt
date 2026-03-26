package com.wasmo.installedapps

import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import okio.ByteString
import okio.FileNotFoundException
import okio.FileSystem

@AssistedInject
class FileSystemResourceLoader(
  private val fileSystem: FileSystem,
  @Assisted private val wasmoFileAddress: WasmoFileAddress.FileSystem,
  private val appManifest: AppManifest,
) : ResourceLoader {
  override suspend fun loadManifest() = appManifest

  override suspend fun loadOrNull(resourcePath: String): ByteString? {
    // TODO: honor external_resource paths.

    val path = wasmoFileAddress.path / resourcePath.substring(1)
    return try {
      fileSystem.read(path) {
        readByteString()
      }
    } catch (_: FileNotFoundException) {
      null
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(wasmoFileAddress: WasmoFileAddress.FileSystem): FileSystemResourceLoader
  }
}

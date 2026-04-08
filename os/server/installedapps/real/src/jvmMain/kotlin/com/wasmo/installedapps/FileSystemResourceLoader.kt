package com.wasmo.installedapps

import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.ExternalResource
import com.wasmo.packaging.IncludePattern
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import okio.ByteString
import okio.FileNotFoundException
import okio.FileSystem

@AssistedInject
class FileSystemResourceLoader(
  private val fileSystem: FileSystem,
  private val appManifestLoader: AppManifestLoader,
  @Assisted private val wasmoFileAddress: WasmoFileAddress.FileSystem,
) : ResourceLoader {
  override suspend fun loadOrNull(resourcePath: String): ByteString? {
    val appManifest = appManifestLoader.load()
    for (externalResource in appManifest.external_resource) {
      if (!resourcePath.startsWith(externalResource.to)) continue

      val includePath = resourcePath.substring(externalResource.to.length)
      if (externalResource.includeMatches(includePath)) {
        val loaded = loadResolved(externalResource.from + includePath)
        if (loaded != null) return loaded
      }
    }

    return loadResolved(resourcePath.substring(1))
  }

  private fun ExternalResource.includeMatches(path: String): Boolean =
    include.isEmpty() || include.any { IncludePattern(it).matches(path) }

  private fun loadResolved(resourcePath: String): ByteString? {
    val path = wasmoFileAddress.path / resourcePath
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

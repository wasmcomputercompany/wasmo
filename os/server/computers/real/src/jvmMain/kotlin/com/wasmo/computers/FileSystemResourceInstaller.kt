package com.wasmo.computers

import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.IssueCollector
import com.wasmo.issues.issueCheck
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import dev.eav.tomlkt.decodeFromString
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.SerializationException
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path

@AssistedInject
class FileSystemResourceInstaller(
  private val fileSystem: FileSystem,
  @Assisted private val wasmoFileAddress: WasmoFileAddress.FileSystem,
) : ResourceInstaller {
  context(issueCollector: IssueCollector)
  override suspend fun install(): AppManifest? {
    context(issueCollector.path(wasmoFileAddress.path.toString())) {
      issueCheck(fileSystem.metadataOrNull(wasmoFileAddress.path)?.isDirectory == true) {
        "Not a directory"
      }
    }
    if (issueCollector.issues.isNotEmpty()) return null

    val manifestPath = wasmoFileAddress.path / "wasmo-manifest.toml"
    val appManifest = context(issueCollector.path(manifestPath.toString())) {
      readAppManifest(manifestPath)
    } ?: return null

    context(issueCollector.path(manifestPath.toString())) {
      check(appManifest)
    }
    if (issueCollector.issues.isNotEmpty()) return null

    return appManifest
  }

  context(issueCollector: IssueCollector)
  private suspend fun readAppManifest(manifestPath: Path): AppManifest? {
    try {
      return fileSystem.read(manifestPath) {
        WasmoToml.decodeFromString<AppManifest>(readUtf8())
      }
    } catch (e: FileNotFoundException) {
      issueCollector.path(manifestPath.toString()).add("Manifest file not found", e)
      return null
    } catch (e: SerializationException) {
      issueCollector.path(manifestPath.toString()).add("Decoding manifest failed", e)
      return null
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      wasmoFileAddress: WasmoFileAddress.FileSystem,
    ): FileSystemResourceInstaller
  }
}

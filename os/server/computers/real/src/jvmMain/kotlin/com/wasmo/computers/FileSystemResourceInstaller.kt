package com.wasmo.computers

import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.IssueCollector
import com.wasmo.issues.Severity
import com.wasmo.issues.issueCheck
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.ExternalResource
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
    if (issueCollector.hasFatalIssues) return null

    val manifestPath = wasmoFileAddress.path / "wasmo-manifest.toml"
    val appManifest = context(issueCollector.path(manifestPath.toString())) {
      readAppManifest(manifestPath)
    } ?: return null

    context(issueCollector.path(manifestPath.toString())) {
      AppManifestChecker(allowExternalResources = true).check(appManifest)
    }
    if (issueCollector.hasFatalIssues) return null

    for ((index, value) in appManifest.external_resource.withIndex()) {
      context(issueCollector.href("external_resource[$index]")) {
        checkNotEmpty(value)
      }
    }

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

  context(issueCollector: IssueCollector)
  private suspend fun checkNotEmpty(externalResource: ExternalResource) {
    val from = wasmoFileAddress.path / externalResource.from
    var unmatchedFileCount = 0
    val includePatterns = externalResource.include.map { IncludePattern(it) }

    try {
      for (path in fileSystem.listRecursively(from)) {
        val pathString = path.relativeTo(from).toString()
        when {
          includePatterns.isEmpty() -> return // Empty matches everything.
          includePatterns.any { it.matches(pathString) } -> return
          else -> unmatchedFileCount++
        }
      }
    } catch (_: FileNotFoundException) {
      // Report "No files found" below.
    }

    issueCollector
      .path(from.toString())
      .severity(Severity.Warning)
      .add(
        when {
          unmatchedFileCount > 0 -> "No files match pattern ${externalResource.include}"
          else -> "No files found"
        },
      )
  }

  @AssistedFactory
  interface Factory {
    fun create(
      wasmoFileAddress: WasmoFileAddress.FileSystem,
    ): FileSystemResourceInstaller
  }
}


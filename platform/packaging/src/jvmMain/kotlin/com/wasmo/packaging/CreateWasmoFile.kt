package com.wasmo.packaging

import com.wasmo.issues.IssueCollector
import com.wasmo.issues.Severity
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink

/**
 * Builds a `.wasmo` archive from a local directory.
 */
class CreateWasmoFile(
  private val fileSystem: FileSystem,
  private val inputDirectory: Path,
  private val outputFile: Path,
) {
  context(issueCollector: IssueCollector)
  fun execute() {
    var baseDir = inputDirectory
    var path = inputDirectory / "wasmo-manifest.toml"

    val source = try {
      fileSystem.source(path)
    } catch (_: IOException) {
      baseDir = inputDirectory.resolve("..")
      path = inputDirectory
      fileSystem.source(path)
    }

    val originalAppManifest = try {
      source.buffer().use {
        WasmoToml.decodeFromString<AppManifest>(it.readUtf8())
      }
    } catch (e: SerializationException) {
      issueCollector.path(path.toString()).add("Reading manifest failed", e)
      return
    }

    val checker = AppManifestChecker(allowExternalResources = true)
    checker.check(originalAppManifest)

    if (issueCollector.hasFatalIssues) return

    val updatedAppManifest = originalAppManifest.copy(
      external_resource = listOf(),
    )

    fileSystem.createDirectories(outputFile.resolve("..", normalize = true))
    fileSystem.write(outputFile) {
      ZipOutputStream(outputStream()).use { zipOutputStream ->
        zipOutputStream.putNextEntry(ZipEntry("wasmo-manifest.toml"))
        val wasmoManifestToml = zipOutputStream.sink().buffer()
        wasmoManifestToml.writeUtf8(WasmoToml.encodeToString<AppManifest>(updatedAppManifest))
        wasmoManifestToml.emit()

        writeZipContents(
          zipOutputStream = zipOutputStream,
          appManifest = originalAppManifest,
          baseDir = baseDir,
        )
      }
    }
  }

  context(issueCollector: IssueCollector)
  private fun writeZipContents(
    zipOutputStream: ZipOutputStream,
    appManifest: AppManifest,
    baseDir: Path,
  ) {
    val defaultResource = ExternalResource(from = ".", to = "/")

    for (externalResource in listOf(defaultResource) + appManifest.external_resource) {
      val includePatterns = externalResource.include.map { IncludePattern(it) }
      val fromDir = baseDir.resolve(externalResource.from, normalize = true)

      var fromDirFileCount = 0
      var packagedFileCount = 0

      // Eagerly resolve the otherwise-lazy listRecursively() so we can trigger exceptions.
      val paths = try {
        fileSystem.listRecursively(fromDir).toList()
      } catch (_: IOException) {
        // Report "No files found" below.
        listOf()
      }

      for (path in paths) {
        fromDirFileCount++
        val relativePath = path.relativeTo(fromDir)

        // Skip files that don't match includes.
        if (
          includePatterns.isNotEmpty() &&
          includePatterns.none { it.matches(relativePath.toString()) }
        ) continue

        val toPath = (externalResource.to.removePrefix("/").toPath() / relativePath).toString()

        // Skip wasmo-manifest.toml, we'll write one without an external_resources clause.
        if (toPath == "wasmo-manifest.toml") continue

        try {
          fileSystem.read(path) {
            zipOutputStream.putNextEntry(ZipEntry(toPath))
            readAll(zipOutputStream.sink())
          }
        } catch (_: FileNotFoundException) {
          // Probably a directory entry.
        }

        packagedFileCount++
      }

      if (externalResource != defaultResource && packagedFileCount == 0) {
        issueCollector
          .path(fromDir.toString())
          .severity(Severity.Warning)
          .add(
            when {
              fromDirFileCount > 0 -> "No files match pattern ${externalResource.include}"
              else -> "No files found"
            },
          )
      }
    }
  }
}

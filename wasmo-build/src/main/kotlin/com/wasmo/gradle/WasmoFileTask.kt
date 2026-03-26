package com.wasmo.gradle

import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.sink
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Zips the contents of a directory to create a `.wasmo` file.
 *
 * Later this should confirm the existence of a `wasmo-manifest.toml` file, and include its external
 * resources.
 */
abstract class WasmoFileTask : DefaultTask() {
  @get:InputDirectory
  abstract val inputDirectory: DirectoryProperty

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @get:Input
  abstract val wasmoFilePath: Property<String>

  @TaskAction
  fun task() {
    val fileSystem = FileSystem.SYSTEM
    val inputDirectoryPath = inputDirectory.asFile.get().toOkioPath()
    val outputDirectoryPath = outputDirectory.asFile.get().toOkioPath()
    val outputFilePath = outputDirectoryPath / wasmoFilePath.get()

    fileSystem.deleteRecursively(outputDirectoryPath)
    fileSystem.createDirectories(outputFilePath.parent!!)

    fileSystem.write(outputFilePath) {
      ZipOutputStream(outputStream()).use { zipOutputStream ->
        for (path in fileSystem.listRecursively(inputDirectoryPath)) {
          try {
            fileSystem.read(path) {
              zipOutputStream.putNextEntry(ZipEntry(path.relativeTo(inputDirectoryPath).toString()))
              readAll(zipOutputStream.sink())
            }
          } catch (_: FileNotFoundException) {
            // Probably a directory entry.
          }
        }
      }
    }
  }
}

package com.wasmo.gradle

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Runs the `create-wasmo-file` CLI.
 */
abstract class WasmoFileTask : DefaultTask() {
  @get:Inject
  abstract val execOperations: ExecOperations

  @get:Classpath
  abstract val classpath: ConfigurableFileCollection

  @get:InputDirectory
  abstract val inputDirectory: DirectoryProperty

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @get:Input
  abstract val wasmoFilePath: Property<String>

  init {
    group = "wasmo"
    description = "builds a .wasmo archive from a directory"
  }

  fun setSlug(slug: String) {
    inputDirectory.set(File(project.projectDir, "$slug.wasmo"))
    outputDirectory.set(project.layout.buildDirectory.dir("wasmo/$slug"))
    wasmoFilePath.set("static/$slug/$slug.wasmo")
  }

  @TaskAction
  fun task() {
    execOperations.javaexec {
      classpath(this@WasmoFileTask.classpath)
      mainClass.set("com.wasmo.cli.WasmoCommandKt")
      args = listOf(
        "create-wasmo-file",
        "${inputDirectory.get()}",
        "${outputDirectory.get()}/${wasmoFilePath.get()}",
      )
    }
  }
}

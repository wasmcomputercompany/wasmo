package com.wasmo.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.path
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.CreateWasmoFile
import java.nio.file.Path
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class CreateWasmoFileCommand : CliktCommand() {
  val inputDirectory: Path by argument()
    .path(mustExist = true, canBeFile = true, canBeDir = true)
    .help("either a directory containing a wasmo-manifest.toml file, or a wasmo-manifest.toml file")
  val outputFile: Path by argument()
    .path(canBeDir = false, mustBeWritable = true)
    .help("location of the output .wasmo archive")

  override fun run() {
    val issues = IssueCollector.collect {
      CreateWasmoFile(
        fileSystem = FileSystem.SYSTEM,
        inputDirectory = inputDirectory.toOkioPath(),
        outputFile = outputFile.toOkioPath(),
      ).execute()
    }

    for (issue in issues) {
      println(issue)
    }
  }
}

fun main(args: Array<String>) = CreateWasmoFileCommand().main(args)

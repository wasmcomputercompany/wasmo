package com.wasmo.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

class WasmoCommand : CliktCommand(
  name = "moose",
) {
  override fun run() = Unit
}

fun main(args: Array<String>) = WasmoCommand()
  .subcommands(CreateWasmoFileCommand())
  .main(args)

package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.Export
import com.wasmo.support.wit.ExternalUsePath
import com.wasmo.support.wit.Import
import com.wasmo.support.wit.Include
import com.wasmo.support.wit.Location
import com.wasmo.support.wit.SymbolIndex
import com.wasmo.support.wit.WitFile
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.World
import com.wasmo.support.wit.toPackageName
import kotlin.test.Test
import okio.Path.Companion.toPath

class WorldFlattenerTest {
  @Test
  fun `include relative path`() {
    val command = World(
      name = "command",
      declarations = listOf(
        Include(
          path = "imports",
          items = listOf(),
        ),
        Export(
          location = Location(1, 1),
          value = ExternalUsePath(path = "run"),
        ),
      ),
    )

    val imports = World(
      name = "imports",
      declarations = listOf(
        Import(
          location = Location(1, 1),
          value = ExternalUsePath(path = "exit"),
        ),
      ),
    )

    val wasiCli = "wasi:cli@0.3.0".toPackageName()
    val wasiCommand = WitPackage(
      packageName = wasiCli,
      files = mapOf(
        "command.wit".toPath() to WitFile(
          packageName = wasiCli,
          declarations = listOf(command),
        ),
        "imports.wit".toPath() to WitFile(
          packageName = wasiCli,
          declarations = listOf(imports),
        ),
      ),
    )
    val worldFlattener = WorldFlattener(
      SymbolIndex(listOf(wasiCommand)),
    )

    assertThat(
      worldFlattener.flatten(
        world = command,
        inPackageName = wasiCli,
      ),
    ).isEqualTo(
      World(
        name = "command",
        declarations = listOf(
          Export(
            location = Location(1, 1),
            value = ExternalUsePath(path = "run"),
          ),
          Import(
            location = Location(1, 1),
            value = ExternalUsePath(path = "exit"),
          ),
        ),
      ),
    )
  }
}

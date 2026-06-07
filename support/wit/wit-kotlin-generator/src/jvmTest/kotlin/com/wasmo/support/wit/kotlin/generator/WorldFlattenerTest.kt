package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.Export
import com.wasmo.support.wit.ExternalUsePath
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Import
import com.wasmo.support.wit.Include
import com.wasmo.support.wit.Location
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.SymbolIndex
import com.wasmo.support.wit.UsePath
import com.wasmo.support.wit.WitFile
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.World
import kotlin.test.Test
import okio.Path.Companion.toPath

class WorldFlattenerTest {
  @Test
  fun `include relative path`() {
    val command = World(
      location = Location(1, 1),
      name = Identifier("command"),
      declarations = listOf(
        Include(
          location = Location(1, 1),
          path = UsePath(name = Identifier("imports")),
          items = listOf(),
        ),
        Export(
          location = Location(1, 1),
          value = ExternalUsePath(path = UsePath(Identifier("run"))),
        ),
      ),
    )

    val imports = World(
      location = Location(1, 1),
      name = Identifier("imports"),
      declarations = listOf(
        Import(
          location = Location(1, 1),
          value = ExternalUsePath(path = UsePath(Identifier("exit"))),
        ),
      ),
    )

    val wasiCli = PackageName("wasi", "cli", "0.3.0")
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
        location = Location(1, 1),
        name = Identifier("command"),
        declarations = listOf(
          Export(
            location = Location(1, 1),
            value = ExternalUsePath(path = UsePath(Identifier("run"))),
          ),
          Import(
            location = Location(1, 1),
            value = ExternalUsePath(path = UsePath(Identifier("exit"))),
          ),
        ),
      ),
    )
  }
}

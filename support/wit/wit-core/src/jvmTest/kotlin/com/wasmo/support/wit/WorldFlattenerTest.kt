package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import okio.Path.Companion.toPath

class WorldFlattenerTest {
  @Test
  fun `include relative path`() {
    val command = World(
      name = "command",
      declarations = listOf(Include(path = "imports")),
      exports = listOf(ExternalUsePath(path = "run")),
    )

    val imports = World(
      name = "imports",
      imports = listOf(ExternalUsePath(path = "exit")),
    )

    val wasiCommand = WitPackage(
      packageName = "wasi:cli@0.3.0".toPackageName(),
      files = mapOf(
        "command.wit".toPath() to WitFile(
          declarations = listOf(command),
        ),
        "imports.wit".toPath() to WitFile(
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
        inPackageName = "wasi:cli@0.3.0".toPackageName(),
      ),
    ).isEqualTo(
      World(
        name = "command",
        exports = listOf(ExternalUsePath(path = "run")),
        imports = listOf(ExternalUsePath(path = "exit")),
      ),
    )
  }
}

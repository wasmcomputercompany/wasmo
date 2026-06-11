package com.wasmo.support.wit.ir

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.io.ExternalUsePath
import com.wasmo.support.wit.io.Include
import com.wasmo.support.wit.io.IoWitFile
import com.wasmo.support.wit.io.IoWitPackage
import com.wasmo.support.wit.io.IrExternalUsePath
import com.wasmo.support.wit.io.IrInterfaceName
import com.wasmo.support.wit.io.IrWorld
import com.wasmo.support.wit.io.World
import com.wasmo.support.wit.io.toPackageName
import kotlin.test.Test
import okio.Path.Companion.toPath

class WorldFlattenerTest {
  @Test
  fun `include relative path`() {
    val command = World(
      name = "command",
      items = listOf(Include(path = "imports")),
      exports = listOf(ExternalUsePath(path = "run")),
    )

    val imports = World(
      name = "imports",
      imports = listOf(ExternalUsePath(path = "exit")),
    )

    val wasiCommand = IoWitPackage(
      packageName = "wasi:cli@0.3.0".toPackageName(),
      files = mapOf(
        "command.wit".toPath() to IoWitFile(
          items = listOf(command),
        ),
        "imports.wit".toPath() to IoWitFile(
          items = listOf(imports),
        ),
      ),
    )
    val irMapper = IrMapper(listOf(wasiCommand))
    val mapped = irMapper.map()

    assertThat(
      mapped.single().items.single { (it as? IrWorld)?.name == Identifier("command") },
    ).isEqualTo(
      IrWorld(
        name = "command",
        exports = listOf(IrExternalUsePath(path = IrInterfaceName("wasi:cli@0.3.0", "run"))),
        imports = listOf(IrExternalUsePath(path = IrInterfaceName("wasi:cli@0.3.0", "exit"))),
      ),
    )
  }
}

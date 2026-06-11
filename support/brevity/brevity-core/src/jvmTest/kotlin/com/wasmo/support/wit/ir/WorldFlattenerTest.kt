package com.wasmo.support.wit.ir

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.io.IoExternalApi
import com.wasmo.support.wit.io.IoInclude
import com.wasmo.support.wit.io.IoWitFile
import com.wasmo.support.wit.io.IoWitPackage
import com.wasmo.support.wit.io.IoWorld
import com.wasmo.support.wit.toPackageName
import kotlin.test.Test
import okio.Path.Companion.toPath

class WorldFlattenerTest {
  @Test
  fun `include relative path`() {
    val command = IoWorld(
      name = "command",
      items = listOf(IoInclude(path = "imports")),
      exports = listOf(IoExternalApi(path = "run")),
    )

    val imports = IoWorld(
      name = "imports",
      imports = listOf(IoExternalApi(path = "exit")),
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
        exports = listOf(IrExternalApi(path = IrInterfaceName("wasi:cli@0.3.0", "run"))),
        imports = listOf(IrExternalApi(path = IrInterfaceName("wasi:cli@0.3.0", "exit"))),
      ),
    )
  }
}

package dev.wasmo.brevity.ir

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.io.IoExternalApi
import dev.wasmo.brevity.io.IoFunction
import dev.wasmo.brevity.io.IoInclude
import dev.wasmo.brevity.io.IoInterface
import dev.wasmo.brevity.io.IoWitFile
import dev.wasmo.brevity.io.IoWitPackage
import dev.wasmo.brevity.io.IoWorld
import dev.wasmo.brevity.toPackageName
import kotlin.test.Test
import okio.Path.Companion.toPath

class WorldFlattenerTest {
  /**
   * Note that imports and exports are stripped unless the target interfaces declare at least one
   * function.
   */
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

    val run = IoInterface(
      name = "run",
      items = listOf(
        IoFunction(
          name = "run",
        ),
      ),
    )

    val exit = IoInterface(
      name = "exit",
      items = listOf(
        IoFunction(
          name = "exit",
        ),
      ),
    )

    val wasiCommand = IoWitPackage(
      packageName = "wasi:cli@0.3.0".toPackageName(),
      files = mapOf(
        "command.wit".toPath() to IoWitFile(
          items = listOf(command),
        ),
        "exit.wit".toPath() to IoWitFile(
          items = listOf(exit),
        ),
        "imports.wit".toPath() to IoWitFile(
          items = listOf(imports),
        ),
        "run.wit".toPath() to IoWitFile(
          items = listOf(run),
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
        exports = listOf(
          IrExternalApi(
            packageName = "wasi:cli@0.3.0",
            parentName = "run",
            functions = listOf(
              IrFunction(
                packageName = "wasi:cli@0.3.0",
                parentName = "run",
                name = "run",
              ),
            ),
          ),
        ),
        imports = listOf(
          IrExternalApi(
            packageName = "wasi:cli@0.3.0",
            parentName = "exit",
            functions = listOf(
              IrFunction(
                packageName = "wasi:cli@0.3.0",
                parentName = "exit",
                name = "exit",
              ),
            ),
          ),
        ),
      ),
    )
  }
}

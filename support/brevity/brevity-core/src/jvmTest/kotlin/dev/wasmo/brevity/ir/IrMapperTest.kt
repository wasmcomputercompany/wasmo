package dev.wasmo.brevity.ir

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.Offset
import dev.wasmo.brevity.io.IoTypeName
import dev.wasmo.brevity.io.IoWitPackage
import dev.wasmo.brevity.io.toUsePath
import dev.wasmo.brevity.io.toWitFile
import dev.wasmo.brevity.toPackageName
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Path.Companion.toPath

class IrMapperTest {
  @Test
  fun `find local symbols`() {
    val ioPackages = listOf(
      IoWitPackage(
        packageName = "wasi:clocks".toPackageName(),
        files = mapOf(
          "clock.wit".toPath() to """
            |package wasi:clocks;
            |
            |interface wall-clock {
            |    record datetime {
            |        seconds: u64,
            |    }
            |}
            """.trimMargin().toWitFile(),
        ),
      ),
    )
    val irMapper = IrMapper(ioPackages)

    assertThat(
      irMapper.getType(
        packageName = "wasi:clocks",
        parentName = "wall-clock",
        typeName = IoTypeName.Declared("datetime"),
      ),
    ).isEqualTo(
      IrTypeName.Declared(
        packageName = "wasi:clocks".toPackageName(),
        parentName = Identifier("wall-clock"),
        name = Identifier("datetime"),
      ),
    )

    assertThat(
      assertFailsWith<IllegalArgumentException> {
        irMapper.getType(
          packageName = "wasi:clocks",
          parentName = "wall-clock",
          typeName = IoTypeName.Declared("instant"),
        )
      },
    ).hasMessage("unable to find instant in wasi:clocks/wall-clock")
  }

  @Test
  fun `find symbols across packages with use`() {
    val ioPackages = listOf(
      IoWitPackage(
        packageName = "wasi:cli".toPackageName(),
        files = mapOf(
          "stdio.wit".toPath() to """
            |interface stdin {
            |  use wasi:io/streams@0.2.12.{input-stream};
            |
            |  get-stdin: func() -> input-stream;
            |}
            """.trimMargin().toWitFile(),
        ),
      ),
      IoWitPackage(
        packageName = "wasi:io@0.2.12".toPackageName(),
        files = mapOf(
          "streams.wit".toPath() to """
            |package wasi:io@0.2.12;
            |
            |interface streams {
            |    resource input-stream {
            |        read: func(len: u64) -> result;
            |    }
            |}
            """.trimMargin().toWitFile(),
        ),
      ),
    )
    val irMapper = IrMapper(ioPackages)

    assertThat(
      irMapper.getType(
        packageName = "wasi:cli",
        parentName = "stdin",
        typeName = IoTypeName.Declared("input-stream"),
      ),
    ).isEqualTo(
      IrTypeName.Declared(
        packageName = "wasi:io@0.2.12".toPackageName(),
        parentName = Identifier("streams"),
        name = Identifier("input-stream"),
      ),
    )
  }

  @Test
  fun `imports across packages`() {
    val ioPackages = listOf(
      IoWitPackage(
        packageName = "wasi:cli@0.3.0".toPackageName(),
        files = mapOf(
          "command.wit".toPath() to """
            |package wasi:cli@0.3.0;
            |
            |world command {
            |  include imports;
            |}
            """.trimMargin().toWitFile(),
          "imports.wit".toPath() to """
            |package wasi:cli@0.3.0;
            |
            |world imports {
            |  include wasi:clocks/imports@0.3.0;
            |}
            """.trimMargin().toWitFile(),
        ),
      ),
      IoWitPackage(
        packageName = "wasi:clocks@0.3.0".toPackageName(),
        files = mapOf(
          "world.wit".toPath() to """
            |package wasi:clocks@0.3.0;
            |
            |world imports {
            |  import monotonic-clock;
            |}
            """.trimMargin().toWitFile(),
          "monotonic-clock.wit".toPath() to """
            |package wasi:clocks@0.3.0;
            |
            |interface monotonic-clock {
            |  now: func() -> s64;
            |}
            """.trimMargin().toWitFile(),
        ),
      ),
    )
    val irMapper = IrMapper(ioPackages)
    val irPackages = irMapper.map()

    assertThat(irPackages).containsExactly(
      IrWitPackage(
        packageName = "wasi:cli@0.3.0".toPackageName(),
        items = listOf(
          IrWorld(
            offset = Offset(3, 1),
            name = "command",
            imports = listOf(
              IrExternalApi(
                offset = Offset(4, 3),
                path = IrParentName("wasi:clocks@0.3.0", "monotonic-clock"),
              ),
            ),
          ),
          IrWorld(
            offset = Offset(3, 1),
            name = "imports",
            imports = listOf(
              IrExternalApi(
                offset = Offset(4, 3),
                path = IrParentName("wasi:clocks@0.3.0", "monotonic-clock"),
              ),
            ),
          ),
        ),
      ),
      IrWitPackage(
        packageName = "wasi:clocks@0.3.0".toPackageName(),
        items = listOf(
          IrWorld(
            offset = Offset(3, 1),
            name = "imports",
            imports = listOf(
              IrExternalApi(
                offset = Offset(4, 3),
                path = IrParentName("wasi:clocks@0.3.0", "monotonic-clock"),
              ),
            ),
          ),
          IrInterface(
            offset = Offset(3, 1),
            name = "monotonic-clock",
            items = listOf(
              IrFunction(
                offset = Offset(4, 3),
                name = "now",
                returnType = IrTypeName.S64,
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `find symbols in same package with use`() {
    val ioPackages = listOf(
      IoWitPackage(
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        files = mapOf(
          "timezone.wit".toPath() to """
            |interface timezone {
            |    use wall-clock.{datetime};
            |
            |    display: func(when: datetime);
            |}
            """.trimMargin().toWitFile(),
          "wall-clock.wit".toPath() to """
            |package wasi:io@0.2.12;
            |
            |interface wall-clock {
            |    record datetime {
            |        seconds: u64,
            |        nanoseconds: u32,
            |    }
            |}
            """.trimMargin().toWitFile(),
        ),
      ),
    )

    val irMapper = IrMapper(ioPackages)

    assertThat(
      irMapper.getType(
        packageName = "wasi:clocks@0.2.12",
        parentName = "timezone",
        typeName = IoTypeName.Declared("datetime"),
      ),
    ).isEqualTo(
      IrTypeName.Declared(
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        parentName = Identifier("wall-clock"),
        name = Identifier("datetime"),
      ),
    )
  }

  @Test
  fun `get world`() {
    val wasiCli = IoWitPackage(
      packageName = "wasi:cli@0.2.12".toPackageName(),
      files = mapOf(
        "command.wit".toPath() to """
          |package wasi:cli@0.2.12;
          |
          |world command {
          |}
          """.trimMargin().toWitFile(),
      ),
    )
    val wasiIo = IoWitPackage(
      packageName = "wasi:io@0.2.12".toPackageName(),
      files = mapOf(
        "world.wit".toPath() to """
          |package wasi:io@0.2.12;
          |
          |world imports {
          |}
          """.trimMargin().toWitFile(),
      ),
    )

    val irMapper = IrMapper(listOf(wasiCli, wasiIo))

    assertThat(irMapper.getWorldOrNull("wasi:io/imports@0.2.12".toUsePath()))
      .isEqualTo(wasiIo.files.values.single().items.single())

    assertThat(irMapper.getWorldOrNull("wasi:cli/command@0.2.12".toUsePath()))
      .isEqualTo(wasiCli.files.values.single().items.single())

    assertThat(irMapper.getWorldOrNull("wasi:cli/command".toUsePath())).isNull()
  }

  private fun IrMapper.getType(
    packageName: String,
    parentName: String,
    typeName: IoTypeName,
  ): IrTypeName {
    context(
      IrMapper.Context(
        packageName = packageName.toPackageName(),
        parentName = Identifier(parentName),
      ),
    ) {
      return typeName.typeNameToIr()
    }
  }
}

package com.wasmo.support.wit.ir

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.io.IoTypeName
import com.wasmo.support.wit.io.IoWitPackage
import com.wasmo.support.wit.io.toPackageName
import com.wasmo.support.wit.io.toUsePath
import com.wasmo.support.wit.io.toWitFile
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Path.Companion.toPath

class IrMapperTest {
  @Test
  fun `find local symbols`() {
    val index = IrMapper(
      packages = listOf(
        IoWitPackage(
          packageName = "wasi:clocks".toPackageName(),
          files = mapOf(
            "clock.wit".toPath() to
              """
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
      ),
    )

    assertThat(
      index.getType(
        packageName = "wasi:clocks",
        parentName = "wall-clock",
        typeName = IoTypeName.Declared("datetime"),
      ),
    ).isEqualTo(
      IrTypeName.Declared(
        packageName = "wasi:clocks".toPackageName(),
        interfaceName = Identifier("wall-clock"),
        name = Identifier("datetime"),
      ),
    )

    assertThat(
      assertFailsWith<IllegalArgumentException> {
        index.getType(
          packageName = "wasi:clocks",
          parentName = "wall-clock",
          typeName = IoTypeName.Declared("instant"),
        )
      },
    ).hasMessage("unable to find instant in wasi:clocks/wall-clock")
  }

  @Test
  fun `find symbols across packages with use`() {
    val index = IrMapper(
      packages = listOf(
        IoWitPackage(
          packageName = "wasi:cli".toPackageName(),
          files = mapOf(
            "stdio.wit".toPath() to
              """
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
            "streams.wit".toPath() to
              """
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
      ),
    )

    assertThat(
      index.getType(
        packageName = "wasi:cli",
        parentName = "stdin",
        typeName = IoTypeName.Declared("input-stream"),
      ),
    ).isEqualTo(
      IrTypeName.Declared(
        packageName = "wasi:io@0.2.12".toPackageName(),
        interfaceName = Identifier("streams"),
        name = Identifier("input-stream"),
      ),
    )
  }

  @Test
  fun `find symbols in same package with use`() {
    val index = IrMapper(
      packages = listOf(
        IoWitPackage(
          packageName = "wasi:clocks@0.2.12".toPackageName(),
          files = mapOf(
            "timezone.wit".toPath() to
              """
              |interface timezone {
              |    use wall-clock.{datetime};
              |
              |    display: func(when: datetime);
              |}
              """.trimMargin().toWitFile(),
            "wall-clock.wit".toPath() to
              """
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
      ),
    )

    assertThat(
      index.getType(
        packageName = "wasi:clocks@0.2.12",
        parentName = "timezone",
        typeName = IoTypeName.Declared("datetime"),
      ),
    ).isEqualTo(
      IrTypeName.Declared(
        packageName = "wasi:clocks@0.2.12".toPackageName(),
        interfaceName = Identifier("wall-clock"),
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

    val index = IrMapper(
      packages = listOf(wasiCli, wasiIo),
    )

    assertThat(index.getWorldOrNull("wasi:io/imports@0.2.12".toUsePath()))
      .isEqualTo(wasiIo.files.values.single().items.single())

    assertThat(index.getWorldOrNull("wasi:cli/command@0.2.12".toUsePath()))
      .isEqualTo(wasiCli.files.values.single().items.single())

    assertThat(index.getWorldOrNull("wasi:cli/command".toUsePath())).isNull()
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

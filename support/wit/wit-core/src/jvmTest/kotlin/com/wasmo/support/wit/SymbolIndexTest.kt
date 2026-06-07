package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Path.Companion.toPath

class SymbolIndexTest {
  @Test
  fun `find local symbols`() {
    val index = SymbolIndex(
      packages = listOf(
        WitPackage(
          packageName = PackageName("wasi", "clocks"),
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
        inPackageName = PackageName("wasi", "clocks"),
        inInterfaceName = Identifier("wall-clock"),
        typeName = TypeName.Declared("datetime"),
      ),
    ).isEqualTo(TypePath("wasi", "clocks", "wall-clock", "datetime"))

    assertThat(
      assertFailsWith<IllegalArgumentException> {
        index.getType(
          inPackageName = PackageName("wasi", "clocks"),
          inInterfaceName = Identifier("wall-clock"),
          typeName = TypeName.Declared("instant"),
        )
      },
    ).hasMessage("unable to find instant in wasi:clocks/wall-clock")
  }

  @Test
  fun `find symbols across packages with use`() {
    val index = SymbolIndex(
      packages = listOf(
        WitPackage(
          packageName = PackageName("wasi", "cli"),
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
        WitPackage(
          packageName = PackageName("wasi", "io", "0.2.12"),
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
        inPackageName = PackageName("wasi", "cli"),
        inInterfaceName = Identifier("stdin"),
        typeName = TypeName.Declared("input-stream"),
      ),
    ).isEqualTo(TypePath("wasi", "io", "streams", "input-stream", "0.2.12"))
  }

  @Test
  fun `find symbols in same package with use`() {
    val index = SymbolIndex(
      packages = listOf(
        WitPackage(
          packageName = PackageName("wasi", "clocks", "0.2.12"),
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
        inPackageName = PackageName("wasi", "clocks", "0.2.12"),
        inInterfaceName = Identifier("timezone"),
        typeName = TypeName.Declared("datetime"),
      ),
    ).isEqualTo(TypePath("wasi", "clocks", "wall-clock", "datetime", "0.2.12"))
  }

  @Test
  fun `get world`() {
    val wasiCli = WitPackage(
      packageName = PackageName("wasi", "cli", "0.2.12"),
      files = mapOf(
        "command.wit".toPath() to """
          |package wasi:cli@0.2.12;
          |
          |world command {
          |}
          """.trimMargin().toWitFile(),
      ),
    )
    val wasiIo = WitPackage(
      packageName = PackageName("wasi", "io", "0.2.12"),
      files = mapOf(
        "world.wit".toPath() to """
          |package wasi:io@0.2.12;
          |
          |world imports {
          |}
          """.trimMargin().toWitFile(),
      ),
    )

    val index = SymbolIndex(
      packages = listOf(wasiCli, wasiIo),
    )

    assertThat(
      index.getWorldOrNull(
        UsePath(
          namespaces = listOf(Identifier("wasi")),
          packageNames = listOf(Identifier("io")),
          name = Identifier("imports"),
          version = SemVer("0.2.12"),
        ),
      ),
    ).isEqualTo(wasiIo.files.values.single().declarations.single())

    assertThat(
      index.getWorldOrNull(
        UsePath(
          namespaces = listOf(Identifier("wasi")),
          packageNames = listOf(Identifier("cli")),
          name = Identifier("command"),
          version = SemVer("0.2.12"),
        ),
      ),
    ).isEqualTo(wasiCli.files.values.single().declarations.single())

    assertThat(
      index.getWorldOrNull(
        UsePath(
          namespaces = listOf(Identifier("wasi")),
          packageNames = listOf(Identifier("cli")),
          name = Identifier("command"),
        ),
      ),
    ).isNull()
  }
}

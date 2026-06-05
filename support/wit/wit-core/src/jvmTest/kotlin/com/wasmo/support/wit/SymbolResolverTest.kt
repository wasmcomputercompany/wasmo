package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Path.Companion.toPath

class SymbolResolverTest {
  @Test
  fun `resolve local symbols`() {
    val resolver = SymbolResolver(
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
      resolver.resolveType(
        inPackageName = PackageName("wasi", "clocks"),
        inInterfaceName = Identifier("wall-clock"),
        typeName = TypeName.Declared("datetime"),
      ),
    ).isEqualTo(TypePath("wasi", "clocks", "wall-clock", "datetime"))

    assertThat(
      assertFailsWith<IllegalArgumentException> {
        resolver.resolveType(
          inPackageName = PackageName("wasi", "clocks"),
          inInterfaceName = Identifier("wall-clock"),
          typeName = TypeName.Declared("instant"),
        )
      },
    ).hasMessage("unable to resolve instant in wasi:clocks/wall-clock")
  }

  @Test
  fun `resolve symbols across packages with use`() {
    val resolver = SymbolResolver(
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
      resolver.resolveType(
        inPackageName = PackageName("wasi", "cli"),
        inInterfaceName = Identifier("stdin"),
        typeName = TypeName.Declared("input-stream"),
      ),
    ).isEqualTo(TypePath("wasi", "io", "streams", "input-stream", "0.2.12"))
  }

  @Test
  fun `resolve symbols in same package with use`() {
    val resolver = SymbolResolver(
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
      resolver.resolveType(
        inPackageName = PackageName("wasi", "clocks", "0.2.12"),
        inInterfaceName = Identifier("timezone"),
        typeName = TypeName.Declared("datetime"),
      ),
    ).isEqualTo(TypePath("wasi", "clocks", "wall-clock", "datetime", "0.2.12"))
  }
}

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
    val witFile = """
      |package wasi:clocks;
      |
      |interface wall-clock {
      |    record datetime {
      |        seconds: u64,
      |    }
      |}
      """.trimMargin().toWitFile()

    val resolver = SymbolResolver(
      packages = listOf(
        WitPackage(
          packageName = PackageName("wasi", "clocks"),
          files = mapOf("clock.wit".toPath() to witFile)
        )
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
}

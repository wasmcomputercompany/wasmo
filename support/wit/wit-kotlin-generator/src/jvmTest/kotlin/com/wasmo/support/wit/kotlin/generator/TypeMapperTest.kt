package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.SymbolResolver
import com.wasmo.support.wit.TypeName
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.toWitFile
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.Path.Companion.toPath

class TypeMapperTest {
  @Test
  fun `map types`() {
    val witFile = """
      |package wasi:clocks;
      |
      |interface wall-clock {
      |    record datetime {
      |        seconds: u64,
      |    }
      |}
      """.trimMargin().toWitFile()

    val symbolResolver = SymbolResolver(
      packages = listOf(
        WitPackage(
          packageName = PackageName("wasi", "clocks"),
          files = mapOf("clock.wit".toPath() to witFile)
        )
      ),
    )
    val typeMapper = TypeMapper(
      symbolResolver = symbolResolver,
    )

    val packageTypeMapper = typeMapper.refine(PackageName("wasi", "clocks"))
    val interfaceTypeMapper = packageTypeMapper.refine(Identifier("wall-clock"))

    assertThat(interfaceTypeMapper.map(TypeName.Declared("datetime")))
      .isEqualTo(ClassName("wit", "wall-clock", "datetime"))

    assertThat(
      assertFailsWith<IllegalArgumentException> {
        interfaceTypeMapper.map(TypeName.Declared("instant"))
      },
    ).hasMessage("unable to resolve instant in wasi:clocks/wall-clock")
  }
}

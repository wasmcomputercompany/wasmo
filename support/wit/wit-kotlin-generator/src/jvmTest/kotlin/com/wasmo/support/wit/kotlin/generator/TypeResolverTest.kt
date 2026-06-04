package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.TypeName
import com.wasmo.support.wit.WitReader
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TypeResolverTest {
  @Test
  fun `resolve names`() {
    val witFile = WitReader(
      """
      |package wasi:clocks;
      |
      |interface wall-clock {
      |    record datetime {
      |        seconds: u64,
      |    }
      |}
      """.trimMargin(),
    ).read()

    val root = TypeResolver(
      witFiles = listOf(witFile),
      kotlinPackageName = "com.clocks",
    )

    val packageTypeResolver = root.refine(PackageName("wasi", "clocks"))
    val interfaceTypeResolver = packageTypeResolver.refine(Identifier("wall-clock"))

    assertThat(interfaceTypeResolver.resolveTypeName(TypeName("datetime")))
      .isEqualTo(ClassName("com.clocks", "wall-clock", "datetime"))

    assertThat(
      assertFailsWith<IllegalArgumentException> {
        interfaceTypeResolver.resolveTypeName(TypeName("instant"))
      },
    ).hasMessage("unable to resolve instant in wasi:clocks.wall-clock")
  }
}

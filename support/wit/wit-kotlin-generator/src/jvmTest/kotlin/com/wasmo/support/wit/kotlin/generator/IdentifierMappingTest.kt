package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName
import kotlin.test.Test

class IdentifierMappingTest {
  @Test
  fun `camel case`() {
    assertThat("".toCamelCase(upperCamel = true)).isEqualTo("")
    assertThat("-".toCamelCase(upperCamel = true)).isEqualTo("")
    assertThat("-w--".toCamelCase(upperCamel = true)).isEqualTo("W")
    assertThat("wall-clock".toCamelCase(upperCamel = true)).isEqualTo("WallClock")
    assertThat("WALL-clock".toCamelCase(upperCamel = true)).isEqualTo("WallClock")
    assertThat("wall-CLOCK".toCamelCase(upperCamel = true)).isEqualTo("WallClock")
    assertThat("WALL-CLOCK".toCamelCase(upperCamel = true)).isEqualTo("WallClock")
    assertThat("w123-4567clock".toCamelCase(upperCamel = true)).isEqualTo("W1234567clock")
  }

  @Test
  fun `packageName mapping`() {
    assertThat(PackageName("wasi", "clocks").toKotlin("wit"))
      .isEqualTo("wit.wasi.clocks")
    assertThat(PackageName("wasi", "clocks", "0.2.12").toKotlin("wit"))
      .isEqualTo("wit.wasi.clocks.v0_2_12")
  }

  @Test
  fun `className mapping`() {
    assertThat(
      className(
        packagePrefix = "wit",
        packageName = PackageName("wasi", "clocks"),
        interfaceName = Identifier("wall-clock"),
      ),
    ).isEqualTo(ClassName("wit.wasi.clocks", "WallClock"))

    assertThat(
      className(
        packagePrefix = "wit",
        packageName = null,
        interfaceName = Identifier("wall-clock"),
      ),
    ).isEqualTo(ClassName("wit", "WallClock"))

    assertThat(
      className(
        packagePrefix = "wit",
        packageName = PackageName("wasi", "clocks", "0.2.12"),
        interfaceName = Identifier("wall-clock"),
      ),
    ).isEqualTo(ClassName("wit.wasi.clocks.v0_2_12", "WallClock"))
  }
}

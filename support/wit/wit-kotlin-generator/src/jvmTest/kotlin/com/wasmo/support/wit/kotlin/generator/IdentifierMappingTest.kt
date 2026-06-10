package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.wasmo.support.wit.Scope
import com.wasmo.support.wit.toPackageName
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
    assertThat("wasi:clocks".toPackageName().toKotlin("wit"))
      .isEqualTo("wit.wasi.clocks")
    assertThat("wasi:clocks@0.2.12".toPackageName().toKotlin("wit"))
      .isEqualTo("wit.wasi.clocks.v0_2_12")
  }

  @Test
  fun `className mapping`() {
    assertThat(
      className(
        packagePrefix = "wit",
        Scope(
          packageName = "wasi:clocks",
          interfaceName = "wall-clock",
        ),
      ),
    ).isEqualTo(ClassName("wit.wasi.clocks", "WallClock"))

    assertThat(
      className(
        packagePrefix = "wit",
        Scope(
          packageName = "wasi:clocks@0.2.12",
          interfaceName = "wall-clock",
        ),
      ),
    ).isEqualTo(ClassName("wit.wasi.clocks.v0_2_12", "WallClock"))
  }
}

package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
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
}

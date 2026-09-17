package com.wasmo

import assertk.assertThat
import assertk.assertions.hasMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith
import wasmo.objectstore.Key

class ObjectStoreKeyTest {
  @Test
  fun validateKeyLengthAscii() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("")
      },
    ).hasMessage("key length must be in 1..1024 but was 0: ")
    Key("a")
    Key("a".repeat(1024))
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("a".repeat(1025))
      },
    ).hasMessage("key length must be in 1..1024 but was 1025: ${"a".repeat(1025)}")
  }

  @Test
  fun validateKeyLengthNonAscii() {
    Key("🍩")
    Key("🍩".repeat(256))
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("🍩".repeat(257))
      },
    ).hasMessage("key length must be in 1..1024 but was 1028: ${"🍩".repeat(257)}")
  }

  @Test
  fun validateKeyContent() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("\u0000")
      },
    ).hasMessage("key has invalid code point at 0: 0x0")
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("\u001f")
      },
    ).hasMessage("key has invalid code point at 0: 0x1f")
    Key("\u0020")
    Key("\u007e")
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("\u007f")
      },
    ).hasMessage("key has invalid code point at 0: 0x7f")
    Key("\u0080")
  }
}

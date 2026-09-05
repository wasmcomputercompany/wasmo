package com.wasmo

import assertk.assertThat
import assertk.assertions.hasMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith
import wasmo.objectstore.validateKey
import wit.wasmo.objectstore.Key

class ObjectStoreKeyTest {
  @Test
  fun validateKeyLengthAscii() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("").validateKey()
      },
    ).hasMessage("key length must be in 1..1024 but was 0: ")
    Key("a").validateKey()
    Key("a".repeat(1024)).validateKey()
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("a".repeat(1025)).validateKey()
      },
    ).hasMessage("key length must be in 1..1024 but was 1025: ${"a".repeat(1025)}")
  }

  @Test
  fun validateKeyLengthNonAscii() {
    Key("🍩").validateKey()
    Key("🍩".repeat(256)).validateKey()
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("🍩".repeat(257)).validateKey()
      },
    ).hasMessage("key length must be in 1..1024 but was 1028: ${"🍩".repeat(257)}")
  }

  @Test
  fun validateKeyContent() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("\u0000").validateKey()
      },
    ).hasMessage("key has invalid code point at 0: 0x0")
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("\u001f").validateKey()
      },
    ).hasMessage("key has invalid code point at 0: 0x1f")
    Key("\u0020").validateKey()
    Key("\u007e").validateKey()
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        Key("\u007f").validateKey()
      },
    ).hasMessage("key has invalid code point at 0: 0x7f")
    Key("\u0080").validateKey()
  }
}

package com.wasmo

import assertk.assertThat
import assertk.assertions.hasMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ObjectStoreKeyTest {
  @Test
  fun validateKeyLengthAscii() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        "".validateKey()
      },
    ).hasMessage("key length must be in 1..1024 but was 0: ")
    "a".validateKey()
    "a".repeat(1024).validateKey()
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        "a".repeat(1025).validateKey()
      },
    ).hasMessage("key length must be in 1..1024 but was 1025: ${"a".repeat(1025)}")
  }

  @Test
  fun validateKeyLengthNonAscii() {
    "🍩".validateKey()
    "🍩".repeat(256).validateKey()
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        "🍩".repeat(257).validateKey()
      },
    ).hasMessage("key length must be in 1..1024 but was 1028: ${"🍩".repeat(257)}")
  }

  @Test
  fun validateKeyContent() {
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        "\u0000".validateKey()
      },
    ).hasMessage("key has invalid code point at 0: 0x0")
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        "\u001f".validateKey()
      },
    ).hasMessage("key has invalid code point at 0: 0x1f")
    "\u0020".validateKey()
    "\u007e".validateKey()
    assertThat(
      assertFailsWith<IllegalArgumentException> {
        "\u007f".validateKey()
      },
    ).hasMessage("key has invalid code point at 0: 0x7f")
    "\u0080".validateKey()
  }
}

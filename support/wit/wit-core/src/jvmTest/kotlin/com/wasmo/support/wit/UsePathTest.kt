package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class UsePathTest {
  @Test
  fun `UsePath toString`() {
    assertThat(
      UsePath(
        name = Identifier("the-interface"),
      ).toString()
    ).isEqualTo("the-interface")

    assertThat(
      UsePath(
        namespaces = listOf(Identifier("my")),
        packageNames = listOf(Identifier("dependency")),
        name = Identifier("the-interface"),
        version = SemVer("3.0"),
      ).toString()
    ).isEqualTo("my:dependency/the-interface@3.0")

    assertThat(
      UsePath(
        namespaces = listOf(Identifier("abc"), Identifier("def")),
        packageNames = listOf(Identifier("ghi"), Identifier("jkl")),
        name = Identifier("the-interface"),
        version = SemVer("3.0"),
      ).toString()
    ).isEqualTo("abc:def:ghi/jkl/the-interface@3.0")
  }
}

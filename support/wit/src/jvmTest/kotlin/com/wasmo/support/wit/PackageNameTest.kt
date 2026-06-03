package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class PackageNameTest {
  @Test
  fun `PackageName toString`() {
    assertThat(
      PackageName(
        namespaces = listOf(Identifier("abc"), Identifier("def"), Identifier("ghi")),
        names = listOf(Identifier("jkl"), Identifier("mno"), Identifier("pqr")),
      ).toString()
    ).isEqualTo("abc:def:ghi:jkl/mno/pqr")
  }
}

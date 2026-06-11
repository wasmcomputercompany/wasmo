package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.io.Gate
import kotlin.test.Test

class GateTest {
  @Test
  fun `Gate toString`() {
    assertThat(Gate(since = "1.0").toString())
      .isEqualTo("@since(version = 1.0)")
    assertThat(Gate(unstable = "fancier-foo").toString())
      .isEqualTo("@unstable(feature = fancier-foo)")
    assertThat(Gate(deprecated = "2.0").toString())
      .isEqualTo("@deprecated(version = 2.0)")
    assertThat(
      Gate(
        unstable = "fancier-foo",
        since = "1.0",
        deprecated = "2.0",
      ).toString(),
    ).isEqualTo(
      "@unstable(feature = fancier-foo) @since(version = 1.0) @deprecated(version = 2.0)",
    )
  }
}

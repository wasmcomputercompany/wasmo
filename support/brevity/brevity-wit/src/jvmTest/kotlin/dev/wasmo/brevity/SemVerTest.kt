package dev.wasmo.brevity

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SemVerTest {
  @Test
  fun `SemVer toString`() {
    assertThat(SemVer("3.0").toString()).isEqualTo("3.0")
  }
}

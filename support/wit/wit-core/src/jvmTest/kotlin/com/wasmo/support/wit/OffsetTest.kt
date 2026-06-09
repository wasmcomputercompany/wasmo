package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class OffsetTest {
  @Test
  fun `Offset toString`() {
    assertThat(Offset(13, 12).toString())
      .isEqualTo("13:12")
  }
}

package com.wasmo.support.wit

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class LocationTest {
  @Test
  fun `Location toString`() {
    assertThat(Location(13, 12).toString())
      .isEqualTo("13:12")
  }
}

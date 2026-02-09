package com.wasmo.framework

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.serialization.json.Json

class DynamicPageDataTest {
  @Test
  fun happyPath() {
    val pageData = DynamicPageData(
      Json,
      js("""{"created_at":"1970-01-01T00:00:00Z","lucky_numbers":[3,7,42]}"""),
    )
    assertThat(pageData.get<Instant>("created_at"))
      .isEqualTo(Instant.fromEpochMilliseconds(0L))
    assertThat(pageData.get<List<Int>>("lucky_numbers")).isEqualTo(listOf(3, 7, 42))
    assertThat(pageData.get<List<Int>>("unlucky_numbers")).isNull()
  }
}

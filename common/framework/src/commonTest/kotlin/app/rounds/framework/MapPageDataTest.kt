package app.rounds.framework

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.serialization.json.Json

class MapPageDataTest {
  @Test
  fun happyPath() {
    val createdAt = Instant.fromEpochMilliseconds(0L)
    val luckyNumbers = listOf(3, 7, 42)
    val pageData = MapPageData.Builder(Json)
      .put<Instant>("created_at", createdAt)
      .put<List<Int>>("lucky_numbers", luckyNumbers).build()

    assertThat(pageData.get<Instant>("created_at")).isEqualTo(createdAt)
    assertThat(pageData.get<List<Int>>("lucky_numbers")).isEqualTo(luckyNumbers)
    assertThat(pageData.get<List<Int>>("account")).isNull()
  }
}

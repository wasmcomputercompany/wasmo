package app.rounds.framework

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.html.head
import kotlinx.serialization.json.Json
import okio.Buffer

class MapPageDataHtmlTest {
  @Test
  fun happyPath() {
    val pageData = MapPageData.Builder(Json)
      .put<Instant>("created_at", Instant.fromEpochMilliseconds(0L))
      .put<List<Int>>("lucky_numbers", listOf(3, 7, 42)).build()

    val html = Buffer()
      .apply {
        writeHtml {
          head {
            pageData.write(this)
          }
        }
      }
      .readUtf8()

    assertThat(html).isEqualTo(
      """<html><head><script>document.pageData={"created_at":"1970-01-01T00:00:00Z","lucky_numbers":[3,7,42]};</script></head></html>""",
    )
  }
}

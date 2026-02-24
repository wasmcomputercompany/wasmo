package com.wasmo.api.routes

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import kotlin.test.Test

class UrlTest {
  @Test
  fun encodeAndDecodeUrl() {
    val url = Url(
      scheme = "https",
      topPrivateDomain = "wasmo.dev",
      subdomain = "jesse99",
      path = listOf("invite", "1234"),
      query = listOf(QueryParameter("q", "query-string")),
    )
    val string = "https://jesse99.wasmo.dev/invite/1234?q=query-string"
    assertThat(string.decodeUrl()).isEqualTo(url)
    assertThat(url.encode()).isEqualTo(string)
    assertThat(url.encodePathAndQuery()).isEqualTo("/invite/1234?q=query-string")
  }

  @Test
  fun absentQuery() {
    val url = Url(
      scheme = "https",
      topPrivateDomain = "wasmo.dev",
      path = listOf("invite", "1234"),
    )
    val string = "https://wasmo.dev/invite/1234"
    assertThat(string.decodeUrl()).isEqualTo(url)
    assertThat(url.encode()).isEqualTo(string)
    assertThat(url.encodePathAndQuery()).isEqualTo("/invite/1234")
  }

  /** OkHttp uses '%20', URLSearchParams uses '+'. */
  @Test
  fun spaceEncoding() {
    val url = Url(
      scheme = "https",
      topPrivateDomain = "wasmo.dev",
      query = listOf(QueryParameter("q", "query string")),
    )
    val encodedWithPlus = "https://wasmo.dev/?q=query+string"
    val encodedWithPercent = "https://wasmo.dev/?q=query%20string"
    assertThat(encodedWithPlus.decodeUrl()).isEqualTo(url)
    assertThat(encodedWithPercent.decodeUrl()).isEqualTo(url)
    assertThat(url.encode()).isIn(encodedWithPlus, encodedWithPercent)
  }

  @Test
  fun unknownTopPrivateDomain() {
    val url = Url(
      scheme = "https",
      topPrivateDomain = "www.publicobject.com",
      subdomain = null,
    )
    val string = "https://www.publicobject.com/"
    assertThat(string.decodeUrl()).isEqualTo(url)
    assertThat(url.encode()).isEqualTo(string)
  }
}

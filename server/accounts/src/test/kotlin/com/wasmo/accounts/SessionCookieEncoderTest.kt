package com.wasmo.accounts

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant
import okio.ByteString.Companion.encodeUtf8

class SessionCookieEncoderTest {
  private val encoder = SessionCookieEncoder(
    secret = "password".encodeUtf8(),
  )

  @Test
  fun encodeAndDecodeGoldenValue() {
    val goldenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJ0b2tlbiI6IldBU01PMTIzNCIsImlzc3VlZEF0IjoiMjAyNS0xMi0yNVQyMDoyMjozNFoifQ==" +
      ".SVED0Q6ZowFRZE2lJCTXeQjKdvAlCzGXW_qJyEdQdn8="
    val sessionCookie = SessionCookie(
      token = "WASMO1234",
      issuedAt = Instant.parse("2025-12-25T20:22:34Z"),
    )
    assertThat(encoder.encode(sessionCookie)).isEqualTo(goldenValue)
    assertThat(encoder.decode(goldenValue)).isEqualTo(sessionCookie)
  }

  @Test
  fun tooManyParts() {
    val part4 = "{}".encodeUtf8().base64Url()
    val goldenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJ0b2tlbiI6IldBU01PMTIzNCIsImlzc3VlZEF0IjoiMjAyNS0xMi0yNVQyMDoyMjozNFoifQ=" +
      ".Jt4G4ettbmmkV-NBQ5lvHH7cs4Gb2N6qJDNlbHBvRlQ=" +
      ".$part4"
    assertThat(encoder.decode(goldenValue)).isNull()
  }

  @Test
  fun tooFewParts() {
    val goldenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJ0b2tlbiI6IldBU01PMTIzNCIsImlzc3VlZEF0IjoiMjAyNS0xMi0yNVQyMDoyMjozNFoifQ="
    assertThat(encoder.decode(goldenValue)).isNull()
  }

  @Test
  fun badBase64() {
    val badPart1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9!" +
      ".eyJ0b2tlbiI6IldBU01PMTIzNCIsImlzc3VlZEF0IjoiMjAyNS0xMi0yNVQyMDoyMjozNFoifQ=" +
      ".Jt4G4ettbmmkV-NBQ5lvHH7cs4Gb2N6qJDNlbHBvRlQ="
    assertThat(encoder.decode(badPart1)).isNull()
    val badPart2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJ0b2tlbiI6IldBU01PMTIzNCIsImlzc3VlZEF0IjoiMjAyNS0xMi0yNVQyMDoyMjozNFoifQ*=" +
      ".Jt4G4ettbmmkV-NBQ5lvHH7cs4Gb2N6qJDNlbHBvRlQ="
    assertThat(encoder.decode(badPart2)).isNull()
    val badPart3 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJ0b2tlbiI6IldBU01PMTIzNCIsImlzc3VlZEF0IjoiMjAyNS0xMi0yNVQyMDoyMjozNFoifQ=" +
      ".Jt4G4ettbmmkV-NBQ5lvHH7cs4Gb2N6qJDNlbHBvRlQ=&"
    assertThat(encoder.decode(badPart3)).isNull()
  }

  @Test
  fun badSignatureDifferentPayload() {
    val sessionCookie1 = SessionCookie(
      token = "WASMO1234",
      issuedAt = Instant.parse("2025-12-25T20:22:34Z"),
    )
    val (cookie1Part1, cookie1Part2, cookie1Part3) = encoder.encode(sessionCookie1).split('.')

    val sessionCookie2 = SessionCookie(
      token = "WASMO1235",
      issuedAt = Instant.parse("2025-12-25T20:22:34Z"),
    )
    val (cookie2Part1, cookie2Part2, cookie2Part3) = encoder.encode(sessionCookie2).split('.')

    assertThat(encoder.decode("$cookie1Part1.$cookie1Part2.$cookie2Part3")).isNull()
    assertThat(encoder.decode("$cookie2Part1.$cookie2Part2.$cookie1Part3")).isNull()
  }

  @Test
  fun badSignatureDifferentSecret() {
    val sessionCookie = SessionCookie(
      token = "WASMO1234",
      issuedAt = Instant.parse("2025-12-25T20:22:34Z"),
    )
    val encoder1 = SessionCookieEncoder(
      secret = "password1".encodeUtf8(),
    )
    val encoder2 = SessionCookieEncoder(
      secret = "password2".encodeUtf8(),
    )
    assertThat(encoder1.decode(encoder2.encode(sessionCookie))).isNull()
    assertThat(encoder2.decode(encoder1.encode(sessionCookie))).isNull()
  }
}

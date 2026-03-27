package com.wasmo.framework

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import okhttp3.MediaType.Companion.toMediaType

class ContentTypeDatabaseTest {
  @Test
  fun test() {
    assertThat(ContentTypeDatabase.MDN["logo.png"]).isEqualTo("image/png".toMediaType())
    assertThat(ContentTypeDatabase.MDN["logo.PNG"]).isEqualTo("image/png".toMediaType())
    assertThat(ContentTypeDatabase.MDN["logo.zip.png"]).isEqualTo("image/png".toMediaType())
    assertThat(ContentTypeDatabase.MDN["logo.ping"]).isNull()
    assertThat(ContentTypeDatabase.MDN["logo"]).isNull()
  }
}

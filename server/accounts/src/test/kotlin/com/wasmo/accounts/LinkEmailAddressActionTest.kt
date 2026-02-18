package com.wasmo.accounts

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.Response
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LinkEmailAddressActionTest {
  lateinit var tester: WasmoServiceTester

  @BeforeTest
  fun setUp() {
    tester = WasmoServiceTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() {
    val client = tester.newClient()
    val response = client.linkEmailAddressAction().link(
      request = LinkEmailAddressRequest(
        unverifiedEmailAddress = "jesse@example.com",
      ),
    )
    assertThat(response).isEqualTo(
      Response(
        body = LinkEmailAddressResponse(
          challengeSent = true,
        ),
      ),
    )
  }
}

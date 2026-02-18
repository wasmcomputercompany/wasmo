package com.wasmo.accounts

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.Response
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LinkEmailAddressTest {
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

    val linkResponse = client.linkEmailAddressAction().link(
      request = LinkEmailAddressRequest(
        unverifiedEmailAddress = "jesse@example.com",
      ),
    )
    assertThat(linkResponse).isEqualTo(
      Response(
        body = LinkEmailAddressResponse(
          challengeSent = true,
        ),
      ),
    )

    val confirmResponse = client.confirmEmailAddressAction().confirm(
      request = ConfirmEmailAddressRequest(
        unverifiedEmailAddress = "jesse@example.com",
        challengeCode = "123123",
      ),
    )
    assertThat(confirmResponse).isEqualTo(
      Response(
        body = ConfirmEmailAddressResponse(
          success = true,
          hasMoreAttempts = true,
        ),
      ),
    )
  }
}

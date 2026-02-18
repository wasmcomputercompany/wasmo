package com.wasmo.accounts

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.matches
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.Response
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

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
  fun happyPath() = runTest {
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

    val email = tester.sendEmailService.takeEmail()
    assertThat(email.from).isEqualTo("noreply@wasmo.com")
    assertThat(email.to).isEqualTo("jesse@example.com")
    assertThat(email.subject).matches(Regex("\\QSign in to wasmo.com with code \\E\\d{6}"))

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

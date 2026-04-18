package com.wasmo.emails

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.testing.emails.differentCode
import com.wasmo.testing.emails.extractChallengeCode
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class LinkEmailAddressTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun `sign up`() = runTest {
    val client = tester.newClient()

    val linkResponse = client.call().linkEmailAddress("jesse@example.com")
    assertThat(linkResponse.body.challengeToken).isNotEmpty()

    val email = tester.sendEmailService.takeEmail()
    assertThat(email.from).isEqualTo("noreply@wasmo.com")
    assertThat(email.to).isEqualTo("jesse@example.com")

    val confirmResponse = client.call().confirmEmailAddress(
      emailAddress = "jesse@example.com",
      challengeToken = linkResponse.body.challengeToken,
      challengeCode = email.extractChallengeCode(),
    )
    assertThat(confirmResponse.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.LinkedNew)
    assertThat(confirmResponse.body.account)
      .isNotNull()
  }

  @Test
  fun `link multiple addresses`() = runTest {
    val client = tester.newClient()

    val confirmResponse1 = client.linkAndConfirmEmailAddress("jesse@example.com")
    assertThat(confirmResponse1.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.LinkedNew)
    val confirmResponse2 = client.linkAndConfirmEmailAddress("jessewilson@example.com")
    assertThat(confirmResponse2.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.LinkedNew)
  }

  /** Sign in just means linking an email address that's already linked. */
  @Test
  fun `sign in`() = runTest {
    val signUpClient = tester.newClient()
    signUpClient.linkAndConfirmEmailAddress("jesse@example.com")

    val signInClient = tester.newClient()
    val linkResponse = signInClient.call().linkEmailAddress("jesse@example.com")
    assertThat(linkResponse.body.challengeToken).isNotEmpty()

    val email = tester.sendEmailService.takeEmail()
    assertThat(email.from).isEqualTo("noreply@wasmo.com")
    assertThat(email.to).isEqualTo("jesse@example.com")

    val confirmResponse = signInClient.call().confirmEmailAddress(
      emailAddress = "jesse@example.com",
      challengeToken = linkResponse.body.challengeToken,
      challengeCode = email.extractChallengeCode(),
    )
    assertThat(confirmResponse.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.LinkedExisting)
    assertThat(confirmResponse.body.account)
      .isNotNull()
  }

  @Test
  fun `sign up fails due to wrong challenge code`() = runTest {
    val client = tester.newClient()

    val linkResponse = client.call().linkEmailAddress("jesse@example.com")

    val email = tester.sendEmailService.takeEmail()

    val confirmResponse = client.call().confirmEmailAddress(
      emailAddress = "jesse@example.com",
      challengeToken = linkResponse.body.challengeToken,
      challengeCode = email.extractChallengeCode().differentCode(),
    )
    assertThat(confirmResponse.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.WrongChallengeCode)
    assertThat(confirmResponse.body.account)
      .isNull()
  }

  @Test
  fun `sign up fails due to too many bad codes`() = runTest {
    val client = tester.newClient()

    val linkResponse = client.call().linkEmailAddress("jesse@example.com")

    val email = tester.sendEmailService.takeEmail()

    for (i in 0 until ChallengeAttemptRateLimit.count) {
      client.call().confirmEmailAddress(
        emailAddress = "jesse@example.com",
        challengeToken = linkResponse.body.challengeToken,
        challengeCode = email.extractChallengeCode().differentCode(),
      )
    }

    // We don't attempt the correct code if too many incorrect attempts have been made.
    val confirmResponse = client.call().confirmEmailAddress(
      emailAddress = "jesse@example.com",
      challengeToken = linkResponse.body.challengeToken,
      challengeCode = email.extractChallengeCode(),
    )
    assertThat(confirmResponse.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.TooManyAttempts)
    assertThat(confirmResponse.body.account)
      .isNull()
  }

  @Test
  fun `sign up succeeds after rate limit duration elapses`() = runTest {
    val client = tester.newClient()

    val linkResponse = client.call().linkEmailAddress("jesse@example.com")

    val email = tester.sendEmailService.takeEmail()

    for (i in 0 until ChallengeAttemptRateLimit.count) {
      client.call().confirmEmailAddress(
        emailAddress = "jesse@example.com",
        challengeToken = linkResponse.body.challengeToken,
        challengeCode = email.extractChallengeCode().differentCode(),
      )
    }

    tester.clock.now += ChallengeAttemptRateLimit.duration

    val confirmResponse = client.call().confirmEmailAddress(
      emailAddress = "jesse@example.com",
      challengeToken = linkResponse.body.challengeToken,
      challengeCode = email.extractChallengeCode(),
    )
    assertThat(confirmResponse.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.LinkedNew)
    assertThat(confirmResponse.body.account).isNotNull()
  }

  @Test
  fun `sign up code is pinned to client`() = runTest {
    val client1 = tester.newClient()
    val client1LinkResponse = client1.call().linkEmailAddress("jesse@example.com")
    val client1Email = tester.sendEmailService.takeEmail()

    val client2 = tester.newClient()
    val confirmResponse = client2.call().confirmEmailAddress(
      emailAddress = "jesse@example.com",
      challengeToken = client1LinkResponse.body.challengeToken,
      challengeCode = client1Email.extractChallengeCode(),
    )
    assertThat(confirmResponse.body.decision)
      .isEqualTo(ConfirmEmailAddressResponse.Decision.BadRequest)
    assertThat(confirmResponse.body.account)
      .isNull()
  }
}


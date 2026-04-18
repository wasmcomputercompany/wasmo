package com.wasmo.emails

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.wasmo.accounts.CookieSecret
import com.wasmo.identifiers.AccountId
import com.wasmo.support.tokens.toChallengeCodeOrNull
import kotlin.test.Test
import okio.ByteString.Companion.encodeUtf8

class ChallengeTokenCheckerTest {
  @Test
  fun happyPath() {
    val checker = ChallengeTokenChecker(
      CookieSecret("secret".encodeUtf8()),
    )

    val accountId = AccountId(55L)
    val emailAddress = "jesse@example.com"
    val challengeCode = "123456".toChallengeCodeOrNull()!!
    val challengeToken = checker.create(
      accountId = accountId,
      emailAddress = emailAddress,
      challengeCode = challengeCode,
    )

    // Success.
    assertThat(
      checker.check(
        accountId = accountId,
        emailAddress = emailAddress,
        challengeToken = challengeToken,
        challengeCode = challengeCode,
      ),
    ).isTrue()

    // Wrong AccountId.
    assertThat(
      checker.check(
        accountId = AccountId(56),
        emailAddress = emailAddress,
        challengeToken = challengeToken,
        challengeCode = challengeCode,
      ),
    ).isFalse()

    // Wrong Email Address.
    assertThat(
      checker.check(
        accountId = accountId,
        emailAddress = "jodie@example.com",
        challengeToken = challengeToken,
        challengeCode = challengeCode,
      ),
    ).isFalse()

    // Wrong Challenge Code.
    assertThat(
      checker.check(
        accountId = accountId,
        emailAddress = "jodie@example.com",
        challengeToken = challengeToken,
        challengeCode = "123457".toChallengeCodeOrNull()!!,
      ),
    ).isFalse()

    // Wrong secret.
    val checker2 = ChallengeTokenChecker(
      CookieSecret("secret1".encodeUtf8()),
    )
    assertThat(
      checker2.check(
        accountId = accountId,
        emailAddress = emailAddress,
        challengeToken = challengeToken,
        challengeCode = challengeCode,
      ),
    ).isFalse()
  }

  @Test
  fun onlyCorrectCodeMatches() {
    val checker = ChallengeTokenChecker(
      CookieSecret("secret".encodeUtf8()),
    )

    val accountId = AccountId(55L)
    val emailAddress = "jesse@example.com"
    val challengeCode = "123456".toChallengeCodeOrNull()!!
    val challengeToken = checker.create(
      accountId = accountId,
      emailAddress = emailAddress,
      challengeCode = challengeCode,
    )

    // Spend 500ms doing an exhaustive search!
    for (i in 0 until 1_000_000L) {
      assertThat(
        checker.check(
          accountId = accountId,
          emailAddress = emailAddress,
          challengeToken = challengeToken,
          challengeCode = i.toString().padStart(6, '0').toChallengeCodeOrNull()!!,
        ),
      ).isEqualTo(i == 123456L)
    }
  }
}

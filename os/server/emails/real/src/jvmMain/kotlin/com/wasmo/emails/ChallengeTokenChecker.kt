package com.wasmo.emails

import com.wasmo.accounts.CookieSecret
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.OsScope
import com.wasmo.support.tokens.ChallengeCode
import com.wasmo.support.tokens.TokenLength
import com.wasmo.support.tokens.newToken
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

@Inject
@SingleIn(OsScope::class)
class ChallengeTokenChecker(
  private val cookieSecret: CookieSecret,
) {
  fun create(
    accountId: AccountId,
    emailAddress: String,
    challengeCode: ChallengeCode,
  ): String {
    val salt = newToken()
    val hmac = hmac(
      salt = salt,
      accountId = accountId,
      emailAddress = emailAddress,
      challengeCode = challengeCode,
    )
    return "$salt.${hmac.base64().trimEnd('=')}"
  }

  /** Callers should acquire a permit first. */
  fun check(
    accountId: AccountId,
    emailAddress: String,
    challengeToken: String,
    challengeCode: ChallengeCode,
  ): Boolean {
    val parts = challengeToken.split(".", limit = 2)
    if (parts.size != 2) return false

    val salt = parts[0]
    if (salt.length != TokenLength) return false

    val hmac = hmac(
      salt = salt,
      accountId = accountId,
      emailAddress = emailAddress,
      challengeCode = challengeCode,
    )

    val expected = "$salt.${hmac.base64().trimEnd('=')}"
    return challengeToken == expected
  }

  private fun hmac(
    salt: String,
    accountId: AccountId,
    emailAddress: String,
    challengeCode: ChallengeCode,
  ): ByteString {
    val content = "$salt\n${accountId.id}\n$emailAddress\n${challengeCode.value}"
    return content.encodeUtf8().hmacSha256(cookieSecret.value)
  }
}

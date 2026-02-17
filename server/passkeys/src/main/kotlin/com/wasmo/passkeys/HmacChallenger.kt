package com.wasmo.passkeys

import com.wasmo.api.CHALLENGE_LIFETIME
import com.wasmo.api.CHALLENGE_LIFETIME_MAX_STALE
import kotlin.time.Clock
import kotlin.time.Instant
import okio.Buffer
import okio.ByteString

/**
 * Encode and decode our passkey challenges.
 *
 * Our structure is fixed-width and not forwards-compatible.
 *
 * * 25 byte cookie token
 * * 8 byte timestamp
 * * 32 byte HMAC
 *
 * Challenges are only valid for the same cookie token.
 *
 * Challenges are only valid for [CHALLENGE_LIFETIME] plus [CHALLENGE_LIFETIME_MAX_STALE].
 */
class HmacChallenger private constructor(
  val clock: Clock,
  private val cookieSecret: ByteString,
  private val cookieToken: String,
) : Challenger {
  override fun create(): ByteString = encode(Challenge(cookieToken, clock.now()))

  override fun check(challenge: ByteString) {
    val decoded = decode(challenge)
      ?: throw UnexpectedChallengeException("decode failed")

    if (decoded.cookieToken != cookieToken) {
      throw UnexpectedChallengeException("cookie mismatch")
    }

    if (decoded.issuedAt + CHALLENGE_LIFETIME + CHALLENGE_LIFETIME_MAX_STALE <= clock.now()) {
      throw UnexpectedChallengeException("expired")
    }
  }

  internal fun encode(content: Challenge): ByteString {
    val signature = sign(content)
    return Buffer()
      .writeUtf8(content.cookieToken)
      .writeLong(content.issuedAt.toEpochMilliseconds())
      .write(signature)
      .readByteString()
  }

  internal fun decode(challenge: ByteString): Challenge? {
    if (challenge.size != 25 + 8 + 32) return null

    val buffer = Buffer().write(challenge)
    val cookieToken = buffer.readUtf8(25)
    val issuedAt = Instant.fromEpochMilliseconds(buffer.readLong())
    val candidate = Challenge(cookieToken, issuedAt)
    val actualSignature = buffer.readByteString(32)

    return when {
      actualSignature == sign(candidate) -> candidate
      else -> null
    }
  }

  private fun sign(content: Challenge): ByteString {
    return Buffer()
      .writeUtf8(content.cookieToken)
      .writeLong(content.issuedAt.toEpochMilliseconds())
      .hmacSha256(cookieSecret)
  }

  class Factory(
    private val clock: Clock,
    private val cookieSecret: ByteString,
  ) {
    fun create(cookieToken: String): Challenger = HmacChallenger(
      clock = clock,
      cookieSecret = cookieSecret,
      cookieToken = cookieToken,
    )
  }
}

internal class UnexpectedChallengeException(message: String) : RuntimeException(message)

internal data class Challenge(
  val cookieToken: String,
  val issuedAt: Instant,
) {
  init {
    check(cookieToken.length == 25)
  }
}

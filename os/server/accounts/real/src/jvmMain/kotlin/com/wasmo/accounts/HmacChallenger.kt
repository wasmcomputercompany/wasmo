package com.wasmo.accounts

import com.wasmo.api.CHALLENGE_LIFETIME
import com.wasmo.api.CHALLENGE_LIFETIME_MAX_STALE
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Clock
import kotlin.time.Instant
import okio.Buffer
import okio.ByteString

@AssistedInject
class HmacChallenger(
  private val clock: Clock,
  private val cookieSecret: CookieSecret,
  @Assisted private val cookieToken: String,
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
      .hmacSha256(cookieSecret.value)
  }

  @AssistedFactory
  interface Factory {
    fun create(cookieToken: String): HmacChallenger
  }
}

internal data class Challenge(
  val cookieToken: String,
  val issuedAt: Instant,
) {
  init {
    check(cookieToken.length == 25)
  }
}

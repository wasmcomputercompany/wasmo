package com.wasmo.accounts

import com.wasmo.api.CHALLENGE_LIFETIME
import com.wasmo.api.CHALLENGE_LIFETIME_MAX_STALE
import okio.ByteString

/**
 * Encode and decode cookie-scoped challenges.
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
interface Challenger {
  fun create(): ByteString
  fun check(challenge: ByteString)
}

class UnexpectedChallengeException(message: String) : RuntimeException(message)

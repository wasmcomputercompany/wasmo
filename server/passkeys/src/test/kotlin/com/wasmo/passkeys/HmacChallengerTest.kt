package com.wasmo.passkeys

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.FakeClock
import com.wasmo.api.CHALLENGE_LIFETIME
import com.wasmo.api.CHALLENGE_LIFETIME_MAX_STALE
import kotlin.test.Test
import kotlin.test.assertFailsWith
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

class HmacChallengerTest {
  private val clock = FakeClock()
  private val hmacChallengerFactory = HmacChallenger.Factory(
    clock = clock,
    cookieSecret = "secret".encodeUtf8(),
  )

  @Test
  fun validChallenge() {
    val challenger = hmacChallengerFactory.create("1234512345123451234512345")
    val challenge = "313233343531323334353132333435313233343531323334350000000000000000dda4ac191c5a7be286a89d80455a0de702f58b1ec73670949712eda34b007f65".decodeHex()
    assertThat(challenger.create()).isEqualTo(challenge)
    challenger.check(challenge)
  }

  @Test
  fun challengeForDifferentCookie() {
    val challenger = hmacChallengerFactory.create("1234512345123451234512346")
    val challenge = "313233343531323334353132333435313233343531323334350000000000000000dda4ac191c5a7be286a89d80455a0de702f58b1ec73670949712eda34b007f65".decodeHex()
    assertFailsWith<UnexpectedChallengeException> {
      challenger.check(challenge)
    }
  }

  @Test
  fun expiredChallenge() {
    val challenger = hmacChallengerFactory.create("1234512345123451234512345")
    val challenge = "313233343531323334353132333435313233343531323334350000000000000000dda4ac191c5a7be286a89d80455a0de702f58b1ec73670949712eda34b007f65".decodeHex()
    challenger.check(challenge)
    clock.now += CHALLENGE_LIFETIME
    challenger.check(challenge)
    clock.now += CHALLENGE_LIFETIME_MAX_STALE
    assertFailsWith<UnexpectedChallengeException> {
      challenger.check(challenge)
    }
  }

  @Test
  fun malformedChallenge() {
    val challenger = hmacChallengerFactory.create("1234512345123451234512345")
    val challenge = "313233343531323334353132333435313233343531323334350000000000000000dda4ac191c5a7be286a89d80455a0de702f58b1ec73670949712eda34b007f".decodeHex()
    assertFailsWith<UnexpectedChallengeException> {
      challenger.check(challenge)
    }
  }

  @Test
  fun badSignature() {
    val challenger = hmacChallengerFactory.create("1234512345123451234512345")
    val challenge = "313233343531323334353132333435313233343531323334350000000000000000dda4ac191c5a7be286a89d80455a0de702f58b1ec73670949712eda34b007f64".decodeHex()
    assertFailsWith<UnexpectedChallengeException> {
      challenger.check(challenge)
    }
  }
}

package com.wasmo.testing

import com.wasmo.accounts.Challenger
import com.wasmo.accounts.UnexpectedChallengeException
import okio.Buffer
import okio.ByteString

class FakeChallenger(
  callerToken: String,
) : Challenger {
  private val onlyChallenge = Buffer()
    .writeUtf8("FakeChallenger-")
    .writeUtf8(callerToken)
    .readByteString()

  override fun create(): ByteString {
    return onlyChallenge
  }

  override fun check(challenge: ByteString) {
    if (challenge != onlyChallenge) {
      throw UnexpectedChallengeException("challenge mismatch")
    }
  }
}

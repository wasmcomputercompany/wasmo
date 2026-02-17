package com.wasmo.passkeys

import okio.ByteString

interface Challenger {
  fun create(): ByteString
  fun check(challenge: ByteString)
}

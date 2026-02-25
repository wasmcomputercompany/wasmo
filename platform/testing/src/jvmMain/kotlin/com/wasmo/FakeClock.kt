package com.wasmo

import kotlin.time.Clock
import kotlin.time.Instant

class FakeClock : Clock {
  var now: Instant = Instant.fromEpochMilliseconds(0L)
  override fun now() = now
}

package app.rounds.common.testing

import kotlin.time.Clock
import kotlin.time.Instant

class FakeClock : Clock {
  var now: Instant = Instant.fromEpochMilliseconds(0L)
  override fun now() = now
}

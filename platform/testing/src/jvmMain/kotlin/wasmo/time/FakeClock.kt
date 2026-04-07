package wasmo.time

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Inject
@SingleIn(OsScope::class)
class FakeClock : Clock {
  var now: Instant = Instant.fromEpochMilliseconds(0L)
  override fun now() = now

  /** Advance the time an arbitrary amount and return the new time. */
  fun tick() : Instant {
    now += 1.minutes
    return now
  }
}

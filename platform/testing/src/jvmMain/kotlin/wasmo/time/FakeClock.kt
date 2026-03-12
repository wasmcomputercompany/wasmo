package wasmo.time

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Instant

@Inject
@SingleIn(AppScope::class)
class FakeClock : Clock {
  var now: Instant = Instant.fromEpochMilliseconds(0L)
  override fun now() = now
}

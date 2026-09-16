package wit.wasi.clocks.v0_2_0

import com.wasmo.assertRoundTrip
import kotlin.test.Test
import kotlin.time.Instant

class RealAdaptersTest {
  @Test
  fun wallClockDatetime() {
    RealAdapters.wallClockDatetime.assertRoundTrip(
      WallClock.Datetime(0UL, 0U),
      Instant.fromEpochSeconds(0L),
    )
    RealAdapters.wallClockDatetime.assertRoundTrip(
      WallClock.Datetime((-1L).toULong(), 234_000_000U),
      Instant.fromEpochSeconds(-1L, 234_000_000),
    )
    RealAdapters.wallClockDatetime.assertRoundTrip(
      WallClock.Datetime(1L.toULong(), 234_000_000U),
      Instant.fromEpochSeconds(1L, 234_000_000),
    )
    RealAdapters.wallClockDatetime.assertRoundTrip(
      WallClock.Datetime(1_789_561_726UL, 184_208_000U),
      Instant.parse("2026-09-16T12:28:46.184208Z"),
    )
  }
}

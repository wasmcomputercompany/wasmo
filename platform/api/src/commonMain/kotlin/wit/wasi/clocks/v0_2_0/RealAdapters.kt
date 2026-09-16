package wit.wasi.clocks.v0_2_0

import dev.wasmo.brevity.WitAdapter
import kotlin.time.Instant

internal object RealAdapters : Adapters {
  override val wallClockDatetime = object : WitAdapter<WallClock.Datetime, Instant> {
    override fun fromWit(wit: WallClock.Datetime) =
      Instant.fromEpochSeconds(wit.seconds.toLong(), wit.nanoseconds.toInt())

    override fun toWit(value: Instant) =
      WallClock.Datetime(value.epochSeconds.toULong(), value.nanosecondsOfSecond.toUInt())
  }
}

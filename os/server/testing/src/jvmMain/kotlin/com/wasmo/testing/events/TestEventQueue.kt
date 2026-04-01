package com.wasmo.testing.events

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout

@Inject
@SingleIn(AppScope::class)
class TestEventQueue {
  val events = Channel<Event>(capacity = Int.MAX_VALUE)

  suspend fun send(event: Event) {
    events.send(event)
  }

  suspend fun receive(): Event {
    return withTimeout(1.seconds) {
      events.receive()
    }
  }
}

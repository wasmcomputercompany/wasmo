package com.wasmo.testing

import com.wasmo.events.Event
import com.wasmo.events.EventListener
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class FakeEventListener : EventListener {
  val events = ArrayDeque<Event>()

  override fun onEvent(event: Event) {
    events += event
  }

  fun takeEvent(): Event {
    return events.removeFirst()
  }
}

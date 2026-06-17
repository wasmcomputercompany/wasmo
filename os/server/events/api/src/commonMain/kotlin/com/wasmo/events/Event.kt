package com.wasmo.events

import com.wasmo.identifiers.Event

fun interface EventListener {
  fun onEvent(event: Event)
}

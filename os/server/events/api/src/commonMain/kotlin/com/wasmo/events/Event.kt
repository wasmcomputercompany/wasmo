package com.wasmo.events

import com.wasmo.identifiers.Event

interface EventListener {
  fun onEvent(event: Event)
}

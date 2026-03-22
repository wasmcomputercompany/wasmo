package com.wasmo.events

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug

interface EventListener {
  fun onEvent(event: Event)
}

sealed interface Event {
  val computerSlug: ComputerSlug?
    get() = null
  val appSlug: AppSlug?
    get() = null
  val exception: Throwable?
    get() = null
}

data class InstallAppEvent(
  override val computerSlug: ComputerSlug,
  override val appSlug: AppSlug,
  override val exception: Throwable? = null,
) : Event

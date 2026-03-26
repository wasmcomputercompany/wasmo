package com.wasmo.events

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.issues.Issue

interface EventListener {
  fun onEvent(event: Event)
}

sealed interface Event {
  val computerSlug: ComputerSlug?
    get() = null
  val appSlug: AppSlug?
    get() = null
  val issues: List<Issue>
    get() = listOf()
}

data class InstallAppEvent(
  override val computerSlug: ComputerSlug,
  override val appSlug: AppSlug,
  override val issues: List<Issue> = listOf(),
) : Event

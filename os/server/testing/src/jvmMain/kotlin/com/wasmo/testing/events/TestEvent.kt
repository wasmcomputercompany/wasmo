package com.wasmo.testing.events

import com.wasmo.events.Event
import com.wasmo.identifiers.AppSlug

data class AfterInstallEvent(
  override val appSlug: AppSlug,
  val oldVersion: Long,
  val newVersion: Long,
) : Event

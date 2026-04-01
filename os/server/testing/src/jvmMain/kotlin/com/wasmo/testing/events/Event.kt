package com.wasmo.testing.events

import com.wasmo.identifiers.AppSlug

interface Event

data class AfterInstallEvent(
  val appSlug: AppSlug,
  val oldVersion: Long,
  val newVersion: Long,
) : Event

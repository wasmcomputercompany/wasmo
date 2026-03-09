package com.wasmo.events

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug

data class Event(
  val appSlug: AppSlug,
  val computerSlug: ComputerSlug,
) {
}

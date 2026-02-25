package com.wasmo.stripe

import kotlin.time.Instant

data class SubscriptionSnapshot(
  val slug: String,
  val currentAllocation: ComputerAllocationSnapshot,
)

data class ComputerAllocationSnapshot(
  val activeStart: Instant,
  val activeEnd: Instant,
)

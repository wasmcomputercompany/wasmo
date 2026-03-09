package com.wasmo.payments

import com.wasmo.identifiers.ComputerSlug
import kotlin.time.Instant

data class SubscriptionSnapshot(
  val slug: ComputerSlug,
  val currentAllocation: ComputerAllocationSnapshot,
)

data class ComputerAllocationSnapshot(
  val activeStart: Instant,
  val activeEnd: Instant,
)

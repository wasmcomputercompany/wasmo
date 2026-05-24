package com.wasmo.payments

import com.wasmo.identifiers.ComputerSlug
import kotlin.time.Instant

data class SubscriptionSnapshot(
  val slug: ComputerSlug,
  val currentSubscriptionPeriod: SubscriptionPeriodSnapshot,
)

data class SubscriptionPeriodSnapshot(
  val activeStart: Instant,
  val activeEnd: Instant,
)

package com.wasmo.db.computers

import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Instant

data class ComputerAllocation(
  val id: ComputerAllocationId,
  val created_at: Instant,
  val version: Int,
  val stripe_customer_id: StripeCustomerId,
  val stripe_subscription_id: String,
  val computer_id: ComputerId,
  val active_start: Instant,
  val active_end: Instant,
)

package com.wasmo.db.computers

import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Instant

data class DbComputerAllocation(
  val id: ComputerAllocationId,
  val createdAt: Instant,
  val version: Int,
  val stripeCustomerId: StripeCustomerId,
  val stripeSubscriptionId: String,
  val computerId: ComputerId,
  val activeStart: Instant,
  val activeEnd: Instant,
)

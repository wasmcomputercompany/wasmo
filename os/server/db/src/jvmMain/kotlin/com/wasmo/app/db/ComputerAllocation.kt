package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String
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
) {
  class Adapter(
    val idAdapter: ColumnAdapter<ComputerAllocationId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val stripe_customer_idAdapter: ColumnAdapter<StripeCustomerId, Long>,
    val computer_idAdapter: ColumnAdapter<ComputerId, Long>,
    val active_startAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val active_endAdapter: ColumnAdapter<Instant, OffsetDateTime>,
  )
}

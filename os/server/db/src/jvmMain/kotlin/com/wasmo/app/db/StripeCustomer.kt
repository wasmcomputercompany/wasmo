package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.StripeCustomerId
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class StripeCustomer(
  val id: StripeCustomerId,
  val created_at: Instant,
  val version: Int,
  val stripe_customer_id: String,
  val name: String,
  val email: String,
  val country: String,
  val postal_code: String,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<StripeCustomerId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
  )
}

package com.wasmo.db.payments.stripe

import com.wasmo.identifiers.StripeCustomerId
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
)

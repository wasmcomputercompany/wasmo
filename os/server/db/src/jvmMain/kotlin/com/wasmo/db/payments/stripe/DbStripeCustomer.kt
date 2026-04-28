package com.wasmo.db.payments.stripe

import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Instant

data class DbStripeCustomer(
  val id: StripeCustomerId,
  val createdAt: Instant,
  val version: Int,
  val stripeCustomerId: String,
  val name: String,
  val email: String,
  val country: String,
  val postalCode: String,
)

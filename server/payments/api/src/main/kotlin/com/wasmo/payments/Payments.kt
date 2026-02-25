package com.wasmo.payments

import kotlin.time.Instant

/**
 * This is an abstraction over Stripe's API, and not intended to give us any portability to other
 * payments providers.
 *
 * Instead, its only job is to create indirection for testing.
 */
interface PaymentsService {
  fun createCheckoutSession(
    request: CreateCheckoutSessionRequest,
  ): CreateCheckoutSessionResponse

  fun getCheckoutSession(
    checkoutSessionId: String,
  ): CheckoutSession

  fun getSubscription(
    subscriptionId: String,
  ): Subscription
}

data class CreateCheckoutSessionRequest(
  val computerSpecToken: String,
)

data class CreateCheckoutSessionResponse(
  val clientSecret: String,
)

data class CheckoutSession(
  val status: CheckoutStatus,
  val subscriptionId: String,
)

enum class CheckoutStatus {
  Open,
  Complete,
}

data class Subscription(
  val id: String,
  val computerSpecToken: String,
  val currentPeriodStart: Instant,
  val currentPeriodEnd: Instant,
  val customer: Customer,
)

data class Customer(
  val id: String,
  val name: String,
  val email: String,
  val address: Address,
)

data class Address(
  val country: String,
  val postalCode: String,
)

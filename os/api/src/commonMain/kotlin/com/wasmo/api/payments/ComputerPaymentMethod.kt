package com.wasmo.api.payments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How computers can be paid for.
 */
@Serializable
sealed class ComputerPaymentMethod {
  /** No payment possible - only works for computers that are free. */
  @Serializable
  @SerialName("payment-config-free")
  data object FreeOnly : ComputerPaymentMethod()

  /**
   * Payment possible via Stripe - configured via StripePublishableKey, a dependency injection
   * must be set up separately for distributions that need it.
   */
  @Serializable
  @SerialName("payment-config-stripe")
  data class Stripe(val stripePublishableKey: StripePublishableKey) : ComputerPaymentMethod()
}

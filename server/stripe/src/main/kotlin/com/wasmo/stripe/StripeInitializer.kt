package com.wasmo.stripe

import com.stripe.Stripe
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Stripe's API client uses static singletons. Yuck. This makes sure they're run when then y need
 * to be run.
 */
class StripeInitializer(
  private val stripeCredentials: StripeCredentials,
) {
  private val initialized = AtomicBoolean()

  fun initialize() {
    if (initialized.compareAndSet(false, true)) {
      Stripe.apiKey = stripeCredentials.secretKey
    }
  }

  fun requireInitialized() {
    check(initialized.get()) { "StripeInitializer.initialize() was not called" }
  }
}

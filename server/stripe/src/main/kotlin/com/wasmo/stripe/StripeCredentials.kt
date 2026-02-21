package com.wasmo.stripe

import com.wasmo.api.stripe.StripePublishableKey

data class StripeCredentials(
  val publishableKey: StripePublishableKey,
  val secretKey: String,
) {
  init {
    check(secretKey.startsWith("sk_"))
  }
}

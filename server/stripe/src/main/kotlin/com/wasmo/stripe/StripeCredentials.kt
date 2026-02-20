package com.wasmo.stripe

data class StripeCredentials(
  val publishableKey: String,
  val secretKey: String,
) {
  init {
    check(publishableKey.startsWith("pk_"))
    check(secretKey.startsWith("sk_"))
  }
}

package com.wasmo.api.stripe

import kotlinx.serialization.Serializable

@Serializable
data class StripePublishableKey(
  val publishableKey: String,
) {
  init {
    check(publishableKey.startsWith("pk_"))
  }
}

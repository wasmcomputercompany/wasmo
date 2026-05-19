package com.wasmo.api

import com.wasmo.identifiers.ComputerSlug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateComputerSpecRequest(
  val computerSpecToken: String,
  val slug: ComputerSlug,
)

@Serializable
sealed class CreateComputerSpecResponse {
  @Serializable
  @SerialName("PaymentRequired")
  data class PaymentRequired(val checkoutSessionClientSecret: String) : CreateComputerSpecResponse()

  @Serializable
  @SerialName("Success")
  data object Success : CreateComputerSpecResponse()
}

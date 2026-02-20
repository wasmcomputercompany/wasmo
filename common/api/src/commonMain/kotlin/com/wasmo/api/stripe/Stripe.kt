package com.wasmo.api.stripe

import kotlinx.serialization.Serializable

@Serializable
data object CreateCheckoutSessionRequest

@Serializable
data class CreateCheckoutSessionResponse(
  val clientSecret: String,
)

@Serializable
data class GetSessionStatusRequest(
  val sessionId: String,
)

@Serializable
data class GetSessionStatusResponse(
  val status: String,
  val customer_email: String,
)

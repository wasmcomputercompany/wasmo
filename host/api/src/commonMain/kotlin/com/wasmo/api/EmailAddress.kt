package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class LinkEmailAddressRequest(
  val unverifiedEmailAddress: String,
)

@Serializable
data class LinkEmailAddressResponse(
  val challengeSent: Boolean,
)

@Serializable
data class ConfirmEmailAddressRequest(
  val unverifiedEmailAddress: String,
  val challengeCode: String,
)

@Serializable
data class ConfirmEmailAddressResponse(
  val success: Boolean,
  val hasMoreAttempts: Boolean,
)

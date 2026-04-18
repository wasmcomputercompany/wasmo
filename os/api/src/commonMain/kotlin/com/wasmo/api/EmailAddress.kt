package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class LinkEmailAddressRequest(
  val unverifiedEmailAddress: String,
)

@Serializable
data class LinkEmailAddressResponse(
  val challengeToken: String,
)

@Serializable
data class ConfirmEmailAddressRequest(
  val unverifiedEmailAddress: String,
  val challengeToken: String,
  val challengeCode: String,
)

@Serializable
data class ConfirmEmailAddressResponse(
  val decision: Decision,
  val account: AccountSnapshot?,
) {
  enum class Decision {
    /** Like signing up: a never-before-seen email address was linked. */
    LinkedNew,

    /** Like signing in: an existing email was linked and the caller's cookie switched to it. */
    LinkedExisting,

    BadRequest,
    WrongChallengeCode,
    TooManyAttempts,
  }
}

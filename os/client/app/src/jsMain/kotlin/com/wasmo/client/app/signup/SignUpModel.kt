package com.wasmo.client.app.signup

data class SignUpModel(
  val inFlightCalls: Int = 0,
  val challengeCodeEmailAddress: String? = null,

  val emailAddress: String = "",
  val emailAddressCaption: String,
  val canSubmitEmailAddress: Boolean = false,
  val challengeToken: String? = null,

  val challengeCode: String = "",
  val challengeCodeCaption: String,
  val canSubmitChallengeCode: Boolean = false,
)

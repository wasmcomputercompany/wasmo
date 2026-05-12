package com.wasmo.client.app.signup

import com.wasmo.identifiers.UsernameSlug

data class SignUpModel(
  val inFlightCalls: Int = 0,
  val enterChallengeCode: EnterChallengeCodeModel? = null,
  val enterEmailAddressModel: EnterEmailAddressModel? = null,
  val newAccountWithUsername: NewAccountWithUsernameModel? = null,
  val selectUsernameToSign: SelectUsernameToSignInModel? = null,
)

data class SelectUsernameToSignInModel(
  val usernameOptions: List<UsernameSlug> = listOf(),
  val canCreateUsername: Boolean = false,
  val canSubmit: Boolean = false,
)

data class NewAccountWithUsernameModel(
  val username: String = "",
  val usernameCaption: String = "",
  val canSubmit: Boolean = false,
)

data class EnterEmailAddressModel(
  val emailAddress: String = "",
  val emailAddressCaption: String = "",
  val canSubmit: Boolean = false,
)

data class EnterChallengeCodeModel(
  val challengeToken: String,
  val challengeCode: String = "",
  val challengeCodeEmailAddress: String,
  val challengeCodeCaption: String = "",
  val canSubmit: Boolean = false,
)

package com.wasmo.client.app.signup

import com.wasmo.identifiers.UsernameSlug

data class SignUpModel(
  val inFlightCalls: Int = 0,
  val enterChallengeCode: EnterChallengeCodeModel? = null,
  val enterEmailAddress: EnterEmailAddressModel? = null,
  val newOrExistingAccountWithUsername: NewOrExistingAccountWithUsernameModel? = null,
  val signInWithUsernameAndPassword: SignInWithUsernameAndPasswordModel? = null,
)

/** Select an existing username, sign-in as that. */
data class SignInWithUsernameAndPasswordModel(
  val usernameOptions: List<UsernameSlug> = listOf(),
  // If non-null, the username that has been selected and the part of the password typed so far.
  val passwordEntry: Pair<UsernameSlug, String>?,
  val canCreateUsername: Boolean = false,
  val canSubmit: Boolean = false,
  val errorMessage: String? = null,
)

/** Enter a new username to sign-up and simultaneously sign-in with that username, or sign-in as an existing username. */
data class NewOrExistingAccountWithUsernameModel(
  val username: String = "",
  val usernameHelperText: String = "",
  val password: String = "",
  val isPasswordVisible: Boolean = false,
  val passwordConfirmation: String = "",
  val isPasswordConfirmationVisible: Boolean = false,
  val canSubmit: Boolean = false,
  // An existing username to sign-in as. For example, if user "Jesse.123" already exists
  // and the user has typed username="jesse123", then rather than sign-up as "jesse123",
  // we offer them to sign-in as "Jesse.123".
  val existingUsernameToSignInAs: UsernameSlug? = null,
)

/** Enter an email address. You'll be emailed a challenge code to confirm. */
data class EnterEmailAddressModel(
  val emailAddress: String = "",
  val emailAddressHelperText: String = "",
  val canSubmit: Boolean = false,
)

/** Enter a 6-digit code to complete sign-in. */
data class EnterChallengeCodeModel(
  val challengeToken: String,
  val challengeCode: String = "",
  val challengeCodeEmailAddress: String,
  val challengeCodeHelperText: String = "",
  val canSubmit: Boolean = false,
)

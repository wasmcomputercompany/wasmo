package com.wasmo.client.app.signup

import com.wasmo.identifiers.UsernameSlug


sealed interface SignUpEvent {
  data object ClickSendCode : SignUpEvent

  data object ClickSubmitCode : SignUpEvent

  data class EditEmailAddress(
    val emailAddress: String,
  ) : SignUpEvent

  data class EditChallengeCode(
    val challengeCode: String,
  ) : SignUpEvent

  data class ClickUsername(
    val usernameSlug: UsernameSlug,
  ) : SignUpEvent

  data class EditUsername(
    val username: String,
  ) : SignUpEvent

  data object ClickSignUpWithUsername : SignUpEvent

  data object ClickNewAccount : SignUpEvent
}

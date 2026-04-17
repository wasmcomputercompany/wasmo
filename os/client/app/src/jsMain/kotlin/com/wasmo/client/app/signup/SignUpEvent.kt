package com.wasmo.client.app.signup


sealed interface SignUpEvent {
  object ClickSendCode : SignUpEvent

  object ClickSubmitCode : SignUpEvent

  data class EditEmailAddress(
    val emailAddress: String,
  ) : SignUpEvent

  data class EditChallengeCode(
    val challengeCode: String,
  ) : SignUpEvent
}


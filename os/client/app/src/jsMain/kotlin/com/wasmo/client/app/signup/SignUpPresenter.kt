package com.wasmo.client.app.signup

import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.framework.Presenter
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class SignUpPresenter(
  private val scope: CoroutineScope,
  private val accountDataService: AccountDataService,
) : Presenter<SignUpModel, SignUpEvent> {
  private val mutableModel = MutableStateFlow(
    SignUpModel(
      emailAddressCaption = "We’ll email you a challenge code",
      challengeCodeCaption = "",
    ),
  )

  override val model: StateFlow<SignUpModel>
    get() = mutableModel

  override fun onEvent(event: SignUpEvent) {
    when (event) {
      is SignUpEvent.EditEmailAddress -> {
        mutableModel.update {
          it.copy(
            emailAddress = event.emailAddress,
            canSubmitEmailAddress = event.emailAddress.isNotEmpty(),
          )
        }
      }

      is SignUpEvent.ClickSendCode -> {
        callServer { state ->
          val success = accountDataService.linkEmailAddress(
            unverifiedEmailAddress = state.emailAddress,
          )
          if (success) {
            mutableModel.update {
              it.copy(
                challengeCodeEmailAddress = state.emailAddress,
                challengeCodeCaption = "Enter the code sent to ${state.emailAddress}",
              )
            }
          } else {
            mutableModel.update {
              it.copy(emailAddressCaption = "Something failed")
            }
          }
        }
      }

      is SignUpEvent.EditChallengeCode -> {
        mutableModel.update {
          it.copy(
            challengeCode = event.challengeCode,
          )
        }
      }

      SignUpEvent.ClickSubmitCode -> {
        callServer { state ->
          val challengeCodeEmailAddress = state.challengeCodeEmailAddress
            ?: return@callServer // Race?

          val response = accountDataService.confirmEmailAddress(
            unverifiedEmailAddress = challengeCodeEmailAddress,
            challengeCode = state.challengeCode,
          )

          if (response.success) {
            // TODO: navigate.
          } else if (response.hasMoreAttempts) {
            mutableModel.update {
              it.copy(
                canSubmitChallengeCode = true,
                challengeCodeCaption = "That ain't it. please try again",
              )
            }
          } else {
            mutableModel.update {
              it.copy(
                canSubmitChallengeCode = false,
                challengeCodeCaption = "Too many failed attempts! You're locked out.",
              )
            }
          }
        }
      }
    }
  }

  private fun callServer(block: suspend (SignUpModel) -> Unit) {
    mutableModel.update {
      it.copy(inFlightCalls = it.inFlightCalls + 1)
    }

    val snapshot = mutableModel.value
    scope.launch {
      try {
        block(snapshot)
      } finally {
        mutableModel.update {
          it.copy(inFlightCalls = it.inFlightCalls - 1)
        }
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): SignUpPresenter
  }
}

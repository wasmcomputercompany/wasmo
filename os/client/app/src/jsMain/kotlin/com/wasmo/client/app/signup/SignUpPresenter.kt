package com.wasmo.client.app.signup

import com.wasmo.api.ConfirmEmailAddressResponse.Decision
import com.wasmo.api.routes.HomeRoute
import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Presenter
import com.wasmo.support.tokens.toChallengeCodeOrNull
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
  private val router: Router,
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
          val challengeToken = accountDataService.linkEmailAddress(
            unverifiedEmailAddress = state.emailAddress,
          )
          mutableModel.update {
            it.copy(
              challengeCodeEmailAddress = state.emailAddress,
              challengeCodeCaption = "Enter the code sent to ${state.emailAddress}",
              challengeToken = challengeToken,
            )
          }
        }
      }

      is SignUpEvent.EditChallengeCode -> {
        mutableModel.update {
          it.copy(
            canSubmitChallengeCode = event.challengeCode.toChallengeCodeOrNull() != null,
            challengeCode = event.challengeCode,
          )
        }
      }

      SignUpEvent.ClickSubmitCode -> {
        callServer { state ->
          val challengeToken = state.challengeToken
            ?: return@callServer // Race?
          val challengeCodeEmailAddress = state.challengeCodeEmailAddress
            ?: return@callServer // Race?

          val response = accountDataService.confirmEmailAddress(
            unverifiedEmailAddress = challengeCodeEmailAddress,
            challengeToken = challengeToken,
            challengeCode = state.challengeCode,
          )

          when (response.decision) {
            Decision.LinkedNew,
            Decision.LinkedExisting,
              -> {
              response.account?.let { accountSnapshot ->
                accountDataService.receiveAccountSnapshot(accountSnapshot)
              }
              router.goTo(HomeRoute, TransitionDirection.PUSH)
            }

            Decision.BadRequest -> {
              mutableModel.update {
                it.copy(
                  canSubmitChallengeCode = true,
                  challengeCodeCaption = "Something broke.",
                )
              }
            }

            Decision.WrongChallengeCode -> {
              mutableModel.update {
                it.copy(
                  canSubmitChallengeCode = true,
                  challengeCodeCaption = "That ain't it. Try again.",
                )
              }
            }

            Decision.TooManyAttempts -> {
              mutableModel.update {
                it.copy(
                  canSubmitChallengeCode = true,
                  challengeCodeCaption = "That ain't it. Give up!",
                )
              }
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

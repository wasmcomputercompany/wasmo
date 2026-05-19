package com.wasmo.client.app.signup

import com.wasmo.api.ConfirmEmailAddressResponse.Decision
import com.wasmo.api.CreateUsernameDecision
import com.wasmo.api.LinkUsernameDecision
import com.wasmo.api.SignInSnapshot
import com.wasmo.api.routes.HomeRoute
import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Presenter
import com.wasmo.identifiers.UsernameSlug
import com.wasmo.identifiers.UsernameSlugRegex
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
  signInSnapshot: SignInSnapshot?,
) : Presenter<SignUpModel, SignUpEvent> {
  private val mutableModel = MutableStateFlow(
    // TODO: Make SignInSnapshot responsible for all sign-in options, not only username.
    if (signInSnapshot == null) {
      SignUpModel(
        enterEmailAddress = EnterEmailAddressModel(
          emailAddressHelperText = "We’ll email you a challenge code",
        )
      )
    } else {
      SignUpModel(
        selectUsernameToSignIn = SelectUsernameToSignInModel(
          usernameOptions = signInSnapshot.usernameOptions,
          canCreateUsername = signInSnapshot.canCreateUsername,
          canSubmit = true,
        ),
      )
    }
  )

  override val model: StateFlow<SignUpModel>
    get() = mutableModel

  override fun onEvent(event: SignUpEvent) {
    when (event) {
      is SignUpEvent.EditEmailAddress -> {
        mutableModel.update {
          it.copy(
            enterEmailAddress = it.enterEmailAddress?.copy(
              emailAddress = event.emailAddress,
              canSubmit = event.emailAddress.isNotEmpty(),
            ),
          )
        }
      }

      is SignUpEvent.ClickSendCode -> {
        callServer { state ->
          val emailAddress = state.enterEmailAddress!!.emailAddress
          val challengeToken = accountDataService.linkEmailAddress(
            unverifiedEmailAddress = emailAddress,
          )
          mutableModel.update {
            it.copy(
              enterChallengeCode = EnterChallengeCodeModel(
                challengeCodeEmailAddress = emailAddress,
                challengeCodeHelperText = "Enter the code sent to $emailAddress",
                challengeToken = challengeToken,
              ),
            )
          }
        }
      }

      is SignUpEvent.EditChallengeCode -> {
        mutableModel.update {
          val enterChallengeCode = it.enterChallengeCode ?: return // Race.
          it.copy(
            enterChallengeCode = enterChallengeCode.copy(
              challengeCode = event.challengeCode,
              canSubmit = event.challengeCode.toChallengeCodeOrNull() != null,
            ),
          )
        }
      }

      SignUpEvent.ClickSubmitCode -> {
        callServer { state ->
          val challengeCodeModel = state.enterChallengeCode ?: return@callServer // Race.

          val response = accountDataService.confirmEmailAddress(
            unverifiedEmailAddress = challengeCodeModel.challengeCodeEmailAddress,
            challengeToken = challengeCodeModel.challengeToken,
            challengeCode = challengeCodeModel.challengeCode,
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
                  enterChallengeCode = challengeCodeModel.copy(
                    canSubmit = true,
                    challengeCodeHelperText = "Something broke.",
                  ),
                )
              }
            }

            Decision.WrongChallengeCode -> {
              mutableModel.update {
                it.copy(
                  enterChallengeCode = challengeCodeModel.copy(
                    canSubmit = true,
                    challengeCodeHelperText = "That ain't it. Try again.",
                  ),
                )
              }
            }

            Decision.TooManyAttempts -> {
              mutableModel.update {
                it.copy(
                  enterChallengeCode = challengeCodeModel.copy(
                    canSubmit = true,
                    challengeCodeHelperText = "That ain't it. Give up!",
                  ),
                )
              }
            }
          }
        }
      }

      is SignUpEvent.EditUsername -> {
        mutableModel.update {
          val usernameModel = it.newAccountWithUsername ?: return // Race.
          it.copy(
            newAccountWithUsername = usernameModel.copy(
              username = event.username,
              canSubmit = UsernameSlugRegex.matches(event.username),
            ),
          )
        }
      }

      SignUpEvent.ClickSignUpWithUsername -> {
        callServer { state ->
          val usernameModel = state.newAccountWithUsername ?: return@callServer // Race.
          val response = accountDataService.createUsername(UsernameSlug(usernameModel.username))
          when (response.decision) {
            CreateUsernameDecision.Success -> {
              response.account?.let { accountSnapshot ->
                accountDataService.receiveAccountSnapshot(accountSnapshot)
              }
              router.goTo(HomeRoute, TransitionDirection.PUSH)
            }
            else -> {
              // TODO: Update UI to show an error message
            }
          }
        }
      }

      is SignUpEvent.ClickUsername -> {
        callServer {
          val response = accountDataService.linkUsername(event.usernameSlug)
          when (response.decision) {
            LinkUsernameDecision.Success -> {
              response.account?.let { accountSnapshot ->
                accountDataService.receiveAccountSnapshot(accountSnapshot)
              }
              router.goTo(HomeRoute, TransitionDirection.PUSH)
            }
            else -> {
              // TODO: Update UI to show an error message
            }
          }
        }
      }

      SignUpEvent.ClickNewAccount -> {
        mutableModel.update { state ->
          state.copy(
            newAccountWithUsername = NewAccountWithUsernameModel()
          )
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

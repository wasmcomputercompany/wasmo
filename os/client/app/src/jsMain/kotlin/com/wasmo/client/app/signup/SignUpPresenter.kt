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
import com.wasmo.identifiers.isUsernameValid
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
  private val existingUsernamesToSignInConfig = signInSnapshot?.usernameOptions ?: mapOf()
  private val normalizedToExistingUsername = existingUsernamesToSignInConfig.keys.associateBy { it.normalizedValue }

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
        signInWithUsernameAndPassword = SignInWithUsernameAndPasswordModel(
          usernameOptions = existingUsernamesToSignInConfig.keys.toList(),
          canCreateUsername = signInSnapshot.canCreateUsername,
          canSubmit = true,
          passwordEntry = null, // no account with a potential password has been selected yet
        ),
      )
    }
  )

  override val model: StateFlow<SignUpModel>
    get() = mutableModel

  private fun updateSignInWithUsernameAndPasswordModel(
    update: (SignInWithUsernameAndPasswordModel) -> SignInWithUsernameAndPasswordModel,
  ) = mutableModel.update { signUpModel ->
    signUpModel.copy(
      signInWithUsernameAndPassword = signUpModel.signInWithUsernameAndPassword?.let { update(it) }
    )
  }

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

      is SignUpEvent.EditUsername, is SignUpEvent.EditPassword, is SignUpEvent.EditPasswordConfirmation -> {
        mutableModel.update {
          val usernameModel = it.newOrExistingAccountWithUsername ?: return // Race.
          val username = if (event is SignUpEvent.EditUsername) event.username else usernameModel.username
          val password = if (event is SignUpEvent.EditPassword) event.password else usernameModel.password
          val passwordConfirmation = if (event is SignUpEvent.EditPasswordConfirmation) event.passwordConfirmation else usernameModel.passwordConfirmation

          val isUsernameValid = isUsernameValid(username)
          val signInUsername: UsernameSlug? = if (isUsernameValid) normalizedToExistingUsername[UsernameSlug.normalize(username)] else null
          val isSignIn = signInUsername != null
          val isPasswordVisible = signInUsername == null || existingUsernamesToSignInConfig[signInUsername]!!.isPasswordRequired
          val isPasswordConfirmationVisible = isPasswordVisible && (password != "" || passwordConfirmation != "") && !isSignIn
          it.copy(
            newOrExistingAccountWithUsername = usernameModel.copy(
              username = username,
              password = password,
              isPasswordVisible = isPasswordVisible,
              passwordConfirmation = passwordConfirmation,
              isPasswordConfirmationVisible = isPasswordConfirmationVisible,
              canSubmit = isUsernameValid && (!isPasswordConfirmationVisible || password == passwordConfirmation),
              existingUsernameToSignInAs = signInUsername,
              usernameHelperText = if (isSignIn) "${signInUsername.value} exists" else "",
            ),
          )
        }
      }

      SignUpEvent.ClickSignUpWithUsername -> {
        callServer { state ->
          val usernameModel = state.newOrExistingAccountWithUsername ?: return@callServer // Race.
          val password = if (usernameModel.isPasswordVisible) usernameModel.password else ""
          val response = accountDataService.createUsername(
            username = UsernameSlug(usernameModel.username),
            password = password,
          )
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
        val usernameSlug = event.usernameSlug
        val isPasswordRequired = existingUsernamesToSignInConfig[usernameSlug]?.isPasswordRequired ?: false

        if (isPasswordRequired) {
          // Since the username requires a password to sign in, we show the password entry field,
          // starting from an empty string or whatever partial password had previously been typed.
          updateSignInWithUsernameAndPasswordModel { model ->
            val password = model.passwordEntry?.second
            model.copy(
              passwordEntry = Pair(usernameSlug, password ?: ""),
              canSubmit = true
            )
          }
        } else {
          // clear any previous selection of a username and associated partially-typed password,
          // then sign-in without password for the newly clicked username.
          updateSignInWithUsernameAndPasswordModel { model ->
            model.copy(
              passwordEntry = null
            )
          }
          attemptSignIn(usernameSlug, password = null)
        }
      }

      is SignUpEvent.ClickSignInWithUsernamePassword -> {
        attemptSignIn(event.usernameSlug, event.password)
      }

      SignUpEvent.ClickNewAccount -> {
        mutableModel.update { state ->
          state.copy(
            newOrExistingAccountWithUsername = NewOrExistingAccountWithUsernameModel()
          )
        }
      }
    }
  }

  private fun LinkUsernameDecision.toErrorMessage(): String? = when(this) {
    LinkUsernameDecision.Success -> null
    LinkUsernameDecision.PasswordAuthenticationFailed -> "Authentication failed."
    LinkUsernameDecision.UsernameDeleted -> "Username deleted."
    LinkUsernameDecision.UsernameNotFound -> "Username not found."
    LinkUsernameDecision.TooManyAttempts -> "Rate limit exceeded."
  }

  private fun attemptSignIn(usernameSlug: UsernameSlug, password: String?) =
    callServer {
      val response = accountDataService.linkUsername(usernameSlug, password ?: "")
      val errorMessage = response.decision.toErrorMessage()
      if (errorMessage != null) {
        updateSignInWithUsernameAndPasswordModel { model ->
          model.copy(
            errorMessage = errorMessage,
          )
        }
      }
      if (response.decision == LinkUsernameDecision.Success) {
        response.account?.let { accountSnapshot ->
          accountDataService.receiveAccountSnapshot(accountSnapshot)
        }
        router.goTo(HomeRoute, TransitionDirection.PUSH)
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

package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.client.framework.Presenter
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class SignUpUi(
  @Assisted private val presenter: Presenter<SignUpModel, SignUpEvent>,
) : Ui {

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val state by presenter.model.collectAsState()

    val enterChallengeCodeModel = state.enterChallengeCode
    if (enterChallengeCodeModel != null) {
      EnterChallengeCodeScreen(
        attrs = attrs,
        eventListener = presenter::onEvent,
        challengeCode = enterChallengeCodeModel.challengeCode,
        challengeCodeHelperText = enterChallengeCodeModel.challengeCodeHelperText,
        canSubmit = enterChallengeCodeModel.canSubmit,
        disabled = state.inFlightCalls > 0,
        busy = state.inFlightCalls > 0,
      )
      return
    }

    val enterEmailAddressModel = state.enterEmailAddress
    if (enterEmailAddressModel != null) {
      EnterEmailAddressScreen(
        attrs = attrs,
        eventListener = presenter::onEvent,
        emailAddress = enterEmailAddressModel.emailAddress,
        emailAddressHelperText = enterEmailAddressModel.emailAddressHelperText,
        canSubmit = enterEmailAddressModel.canSubmit,
        disabled = state.inFlightCalls > 0,
        busy = state.inFlightCalls > 0,
      )
      return
    }

    val newOrExistingAccountWithUsernameModel = state.newOrExistingAccountWithUsername
    if (newOrExistingAccountWithUsernameModel != null) {
      NewOrExistingAccountWithUsernameScreen(
        attrs = attrs,
        eventListener = presenter::onEvent,
        username = newOrExistingAccountWithUsernameModel.username,
        usernameHelperText = newOrExistingAccountWithUsernameModel.usernameHelperText,
        password = newOrExistingAccountWithUsernameModel.password,
        isPasswordVisible = newOrExistingAccountWithUsernameModel.isPasswordVisible,
        passwordConfirmation = newOrExistingAccountWithUsernameModel.passwordConfirmation,
        isPasswordConfirmationVisible = newOrExistingAccountWithUsernameModel.isPasswordConfirmationVisible,
        existingUsernameToSignInAs = newOrExistingAccountWithUsernameModel.existingUsernameToSignInAs,
        canSubmit = newOrExistingAccountWithUsernameModel.canSubmit,
        disabled = state.inFlightCalls > 0,
        busy = state.inFlightCalls > 0,
      )
      return
    }

    val selectUsernameToSignInModel = state.signInWithUsernameAndPassword
    if (selectUsernameToSignInModel != null) {
      SignInWithUsernameAndPasswordScreen(
        attrs = attrs,
        eventListener = presenter::onEvent,
        usernameOptions = selectUsernameToSignInModel.usernameOptions,
        passwordEntry = selectUsernameToSignInModel.passwordEntry,
        canCreateUsername = selectUsernameToSignInModel.canCreateUsername,
        canSubmit = selectUsernameToSignInModel.canSubmit,
        busy = state.inFlightCalls > 0,
        errorMessage = selectUsernameToSignInModel.errorMessage,
      )
      return
    }

    error("no screen?")
  }

  @AssistedFactory
  interface Factory {
    fun create(
      presenter: Presenter<SignUpModel, SignUpEvent>,
    ): SignUpUi
  }
}

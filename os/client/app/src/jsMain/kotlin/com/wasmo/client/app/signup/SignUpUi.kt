package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.client.app.LocalFormState
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

    CompositionLocalProvider(LocalFormState provides state.formState) {
      if (state.challengeCodeEmailAddress == null) {
        EnterEmailAddressScreen(
          attrs = attrs,
          eventListener = presenter::onEvent,
          emailAddress = state.emailAddress,
          emailAddressCaption = state.emailAddressCaption,
          canSubmit = state.canSubmitEmailAddress,
        )
      } else {
        EnterChallengeCodeScreen(
          attrs = attrs,
          eventListener = presenter::onEvent,
          challengeCode = state.challengeCode,
          challengeCodeCaption = state.challengeCodeCaption,
          canSubmit = state.canSubmitChallengeCode,
        )
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      presenter: Presenter<SignUpModel, SignUpEvent>,
    ): SignUpUi
  }
}

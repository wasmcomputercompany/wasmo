package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class SignUpUi(
  private val scope: CoroutineScope,
  private val accountDataService: AccountDataService,
) : Ui {
  private var inFlightCalls by mutableIntStateOf(0)
  private var emailAddress by mutableStateOf("")

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val formState = when {
      inFlightCalls == 0 -> FormState.Ready
      else -> FormState.Busy
    }
    CompositionLocalProvider(LocalFormState provides formState) {
      SignUpScreen(
        attrs = attrs,
        eventListener = ::onAuthScreenEvent,
      )
    }
  }

  fun onAuthScreenEvent(event: AuthScreenEvent) {
    scope.launch {
      when (event) {
        is AuthScreenEvent.EditEmailAddress -> {
          emailAddress = event.emailAddress
        }

        is AuthScreenEvent.ClickSendCode -> {
          inFlightCalls++
          try {
            accountDataService.linkEmailAddress(emailAddress)
          } finally {
            inFlightCalls--
          }
        }
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): SignUpUi
  }
}

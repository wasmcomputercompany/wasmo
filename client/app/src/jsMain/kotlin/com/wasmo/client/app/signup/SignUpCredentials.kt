package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpCredentials(
  eventListener: (SignUpCredentialsEvent) -> Unit,
) {
  var emailState by remember { mutableStateOf("jesse@swank.ca") }
  var passkeyState by remember { mutableStateOf("") }

  TextField(
    label = "Email Address",
  ) {
    value(emailState)
    onInput { event ->
      emailState = event.value
    }
  }
  TextField(
    label = "Passkey",
  ) {
    value(passkeyState)
    onInput { event ->
      passkeyState = event.value
    }
  }
  P {
    Text("You can sign in later with either the passkey or the email address. ")
    A(
      href = "https://www.wired.com/story/what-is-a-passkey-and-how-to-use-them/",
      attrs = {
        target(ATarget.Blank)
      },
    ) {
      Text("Learn about passkeys.")
    }
  }
  PrimaryButton(
    attrs = {
      style {
        marginTop(24.px)
        marginBottom(24.px)
      }
      value("Create Account")
      onClick {
        eventListener(
          SignUpCredentialsEvent.CreateAccount(
            email = emailState,
            passkey = passkeyState,
          ),
        )
      }
    },
  )
}

sealed interface SignUpCredentialsEvent {
  data class CreateAccount(
    val email: String,
    val passkey: String,
  ) : SignUpCredentialsEvent
}

package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.compose.TextField
import com.wasmo.identifiers.UsernameSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun NewOrExistingAccountWithUsernameScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpEvent) -> Unit,
  username: String,
  usernameHelperText: String,
  password: String,
  isPasswordVisible: Boolean,
  passwordConfirmation: String,
  isPasswordConfirmationVisible: Boolean,
  disabled: Boolean,
  canSubmit: Boolean,
  busy: Boolean,
  existingUsernameToSignInAs: UsernameSlug? = null,
) {
  PageLayout(
    attrs = attrs,
  ) { contentAttrs ->
    Column(
      attrs = {
        classes("ContentWidth")
        contentAttrs()
      },
    ) {
      H1 {
        Text("New Account")
      }

      TextField(
        label = "Username",
        helperText = usernameHelperText,
        disabled = disabled,
        inputAttrs = {
          defaultValue(username)
          onInput { event ->
            eventListener(SignUpEvent.EditUsername(event.value))
          }
        },
      )

      if (isPasswordVisible) {
        TextField(
          label = if (existingUsernameToSignInAs != null) "Password" else "Password (optional)",
          helperText = "",
          disabled = false,
          inputAttrs = {
            defaultValue(password)
            onInput { event ->
              eventListener(SignUpEvent.EditPassword(event.value))
            }
          },
          type = InputType.Password,
        )
      }

      if (isPasswordConfirmationVisible) {
        TextField(
          label = "Repeat password",
          helperText = "",
          disabled = false,
          inputAttrs = {
            defaultValue(passwordConfirmation)
            onInput { event ->
              eventListener(SignUpEvent.EditPasswordConfirmation(event.value))
            }
          },
          type = InputType.Password,
        )
      }

      Button(
        busy = busy,
        disabled = !canSubmit || disabled,
        attrs = {
          onClick {
            val event = if (existingUsernameToSignInAs == null) {
              SignUpEvent.ClickSignUpWithUsername
            } else {
              SignUpEvent.ClickUsername(existingUsernameToSignInAs)
            }
            eventListener(event)
          }
        },
      ) {
        Text(if (existingUsernameToSignInAs == null) "Sign Up" else "Sign In as ${existingUsernameToSignInAs.value}" )
      }
    }
  }
}

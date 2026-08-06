package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.compose.TextField
import com.wasmo.identifiers.UsernameSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.autoFocus
import org.jetbrains.compose.web.dom.Fieldset
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun SignInWithUsernameAndPasswordScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpEvent) -> Unit,
  usernameOptions: List<UsernameSlug>,
  passwordEntry: Pair<UsernameSlug, String>?,
  canCreateUsername: Boolean,
  canSubmit: Boolean,
  busy: Boolean,
  errorMessage: String? = null,
) {
  val passwordField = remember { mutableStateOf(passwordEntry?.second ?: "") }

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
        Text("Sign In")
      }

      for (usernameSlug in usernameOptions) {
        val isPasswordFieldVisible = passwordEntry?.first == usernameSlug
        Fieldset {
          Button(
            busy = busy,
            disabled = isPasswordFieldVisible,
            attrs = {
              onClick {
                eventListener(SignUpEvent.ClickUsername(usernameSlug))
              }
            },
          ) {
            Text(usernameSlug.value)
          }
          if (isPasswordFieldVisible) {
            if (errorMessage != null) {
              Text(errorMessage)
            }
            TextField(
              helperText = "password",
              disabled = false,
              inputAttrs = {
                defaultValue(passwordField.value)
                onInput { newValue ->
                  passwordField.value = newValue.value
                }
                autoFocus()
              },
              type = InputType.Password,
            )
            Button(
              busy = busy,
              disabled = !canSubmit,
              attrs = {
                onClick {
                  eventListener(SignUpEvent.ClickSignInWithUsernamePassword(
                    usernameSlug = usernameSlug,
                    password = passwordField.value,
                  ))
                }
              }
            ) {
              Text("Sign in as '${usernameSlug.value}'")
            }
          }
        }
      }

      if (passwordEntry == null && errorMessage != null) {
        // If there's a warning message but no password entry field visible above which we can
        // show the warning message, show it at the bottom instead.
        Text(errorMessage)
      }

      if (canCreateUsername) {
        Fieldset {
          Button(
            busy = busy,
            disabled = !canSubmit,
            outline = true,
            attrs = {
              onClick {
                eventListener(SignUpEvent.ClickNewAccount)
              }
            },
          ) {
            Text("New Account")
          }
        }
      }
    }
  }
}

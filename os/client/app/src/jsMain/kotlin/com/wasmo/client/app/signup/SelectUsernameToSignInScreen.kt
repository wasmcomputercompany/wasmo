package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.identifiers.UsernameSlug
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Fieldset
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun SelectUsernameToSignInScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpEvent) -> Unit,
  options: List<UsernameSlug>,
  canCreateUsername: Boolean,
  canSubmit: Boolean,
  busy: Boolean,
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
        Text("Sign In")
      }

      for (usernameSlug in options) {
        Fieldset {
          Button(
            busy = busy,
            disabled = !canSubmit,
            attrs = {
              onClick {
                eventListener(SignUpEvent.ClickUsername(usernameSlug))
              }
            },
          ) {
            Text(usernameSlug.value)
          }
        }
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

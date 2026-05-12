package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.compose.TextField
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun NewAccountWithUsernameScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpEvent) -> Unit,
  username: String,
  usernameCaption: String,
  disabled: Boolean,
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
        Text("New Account")
      }

      TextField(
        label = "Username",
        caption = usernameCaption,
        disabled = disabled,
        inputAttrs = {
          defaultValue(username)
          onInput { event ->
            eventListener(SignUpEvent.EditUsername(event.value))
          }
        },
      )

      Button(
        busy = busy,
        disabled = !canSubmit || disabled,
        attrs = {
          onClick {
            eventListener(SignUpEvent.ClickSignUpWithUsername)
          }
        },
      ) {
        Text("Sign Up")
      }
    }
  }
}

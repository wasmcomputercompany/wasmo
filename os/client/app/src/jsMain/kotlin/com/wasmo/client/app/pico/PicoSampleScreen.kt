package com.wasmo.client.app.pico

import androidx.compose.runtime.Composable
import com.wasmo.client.app.signup.SignUpEvent
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.AutoComplete
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.autoComplete
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Fieldset
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.Small
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
fun PicoSampleScreen(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  emailAddress: String,
  emailAddressCaption: String,
  canSubmit: Boolean,
  eventListener: (SignUpEvent) -> Unit,
) {
  Main(
    attrs = {
      classes("container")
    },
  ) {
    H1 {
      Text("Sign Up")
    }

    Form(
      attrs = {
        attrs()
      },
    ) {
      Fieldset {
        val inputId = rememberNextId()
        Label {
          Text("Email Address")
          Input(
            type = InputType.Email,
            attrs = {
              defaultValue(emailAddress)
              placeholder("name@example.com")
              attr("aria-label", "Email")
              attr("aria-describedby", inputId)
              autoComplete(AutoComplete.email)
              onInput { event ->
                eventListener(SignUpEvent.EditEmailAddress(event.value))
              }
            },
          )
          Small(
            attrs = {
              id(inputId)
            },
          ) {
            Text(emailAddressCaption)
          }

          Button(
            attrs = {
              onClick {
                eventListener(SignUpEvent.ClickSendCode)
              }
              if (!canSubmit) {
                disabled()
              }
            },
          ) {
            Text("Next")
          }
        }
      }
    }
  }
}

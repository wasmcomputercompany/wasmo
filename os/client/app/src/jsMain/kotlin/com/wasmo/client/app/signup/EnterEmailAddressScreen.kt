package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.compose.PicoButton
import com.wasmo.compose.PicoTextField
import com.wasmo.compose.SegmentedProgressBar
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.AutoComplete
import org.jetbrains.compose.web.attributes.autoComplete
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Fieldset
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
fun EnterEmailAddressScreen(
  attrs: AttrsScope<HTMLElement>.() -> Unit = {},
  emailAddress: String,
  emailAddressCaption: String,
  disabled: Boolean,
  canSubmit: Boolean,
  busy: Boolean,
  eventListener: (SignUpEvent) -> Unit,
) {
  PageLayout(
    attrs = attrs,
  ) { contentAttrs ->
    Column(
      attrs = {
        classes("ContentWidth")
        contentAttrs()
      }
    ) {
      H1 {
        Text("Sign Up")
      }

      if (false) {
        SegmentedProgressBar(
          attrs = {
            style {
              marginBottom(8.px)
            }
          },
          stepsCompleted = 1,
          stepCount = 2,
          minGap = 16.px,
          height = 16.px,
        )
      }

      Fieldset {
        PicoTextField(
          label = "Email Address",
          ariaLabel = "Email",
          disabled = disabled,
          caption = emailAddressCaption,
        ) {
          defaultValue(emailAddress)
          placeholder("name@example.com")
          autoComplete(AutoComplete.email)
          onInput { event ->
            eventListener(SignUpEvent.EditEmailAddress(event.value))
          }
        }

        PicoButton(
          busy = busy,
          disabled = !canSubmit || disabled,
          attrs = {
            onClick {
              eventListener(SignUpEvent.ClickSendCode)
            }
          },
        ) {
          Text("Next")
        }
      }
    }
  }
}

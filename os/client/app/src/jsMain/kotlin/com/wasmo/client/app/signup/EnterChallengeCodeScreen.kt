package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.pico.PicoButton
import com.wasmo.client.app.pico.PicoContent
import com.wasmo.client.app.pico.PicoTextField
import com.wasmo.compose.SegmentedProgressBar
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Fieldset
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun EnterChallengeCodeScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpEvent) -> Unit,
  challengeCode: String,
  challengeCodeCaption: String,
  disabled: Boolean,
  canSubmit: Boolean,
  busy: Boolean,
) {
  PicoContent(
    attrs = attrs,
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
        stepsCompleted = 2,
        stepCount = 2,
        minGap = 16.px,
        height = 16.px,
      )
    }

    Fieldset {
      PicoTextField(
        label = "Challenge Code",
        disabled = disabled,
        caption = challengeCodeCaption,
      ) {
        defaultValue(challengeCode)
        onInput { event ->
          eventListener(SignUpEvent.EditChallengeCode(event.value))
        }
      }

      PicoButton(
        busy = busy,
        disabled = !canSubmit || disabled,
        attrs = {
          onClick {
            eventListener(SignUpEvent.ClickSubmitCode)
          }
        },
      ) {
        Text("Sign Up")
      }
    }
  }
}

package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.compose.Button
import com.wasmo.compose.Column
import com.wasmo.compose.PageLayout
import com.wasmo.compose.SegmentedProgressBar
import com.wasmo.compose.TextField
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
        TextField(
          label = "Challenge Code",
          disabled = disabled,
          caption = challengeCodeCaption,
        ) {
          defaultValue(challengeCode)
          onInput { event ->
            eventListener(SignUpEvent.EditChallengeCode(event.value))
          }
        }

        Button(
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
}

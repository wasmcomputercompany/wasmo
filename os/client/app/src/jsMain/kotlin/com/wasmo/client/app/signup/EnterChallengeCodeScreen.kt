package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SmallText
import com.wasmo.client.app.TextField
import com.wasmo.compose.SegmentedProgressBar
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun EnterChallengeCodeScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpEvent) -> Unit,
  challengeCode: String,
  challengeCodeCaption: String,
  canSubmit: Boolean,
) {
  FormScreen(
    attrs = {
      classes("AuthScreen")
      attrs()
    },
  ) {
    SignUpToolbar()

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

    TextField(
      label = "Challenge Code",
    ) {
      defaultValue(challengeCode)
      onInput { event ->
        eventListener(SignUpEvent.EditChallengeCode(event.value))
      }
    }
    SmallText {
      Text(challengeCodeCaption)
    }

    PrimaryButton(
      attrs = {
        style {
          marginTop(24.px)
          marginBottom(24.px)
        }
        onClick {
          eventListener(SignUpEvent.ClickSubmitCode)
        }
        if (!canSubmit) {
          disabled()
        }
      },
    ) {
      Text("Sign Up")
    }
  }
}

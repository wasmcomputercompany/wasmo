package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SecondaryButton
import com.wasmo.client.app.TextField
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement

@Composable
fun SignUpChallengeCode(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpChallengeCodeEvent) -> Unit,
) {
  var challengeCodeState by remember { mutableStateOf("1 2 3 1 2 3") }

  FormScreen(
    attrs = attrs,
  ) {
    SignUpToolbar()
    SignUpSegmentedProgressBar(
      stepsCompleted = 5,
      stepCount = 5,
    )
    P {
      Text("Enter the code we sent to jesse@swank.ca.")
    }
    TextField {
      value(challengeCodeState)
      onInput { event ->
        challengeCodeState = event.value
      }
    }
    PrimaryButton(
      attrs = {
        style {
          marginTop(24.px)
          marginBottom(24.px)
        }
        value("Finish")
        onClick {
          eventListener(
            SignUpChallengeCodeEvent.Finish(
              challengeCode = challengeCodeState,
            ),
          )
        }
      },
    )
    SecondaryButton(
      attrs = {
        value("Change Email")
        onClick {
          eventListener(
            SignUpChallengeCodeEvent.ChangeEmail,
          )
        }
      },
    )
    SecondaryButton(
      attrs = {
        value("Resend Code")
        onClick {
          eventListener(
            SignUpChallengeCodeEvent.ResendCode,
          )
        }
      },
    )
  }
}

sealed interface SignUpChallengeCodeEvent {
  data class Finish(
    val challengeCode: String,
  ) : SignUpChallengeCodeEvent

  object ChangeEmail : SignUpChallengeCodeEvent
  object ResendCode : SignUpChallengeCodeEvent
}

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
fun EnterEmailAddressScreen(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  emailAddress: String,
  emailAddressCaption: String,
  canSubmit: Boolean,
  eventListener: (SignUpEvent) -> Unit,
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
      stepsCompleted = 1,
      stepCount = 2,
      minGap = 16.px,
      height = 16.px,
    )

    TextField(
      label = "Email Address",
    ) {
      defaultValue(emailAddress)
      onInput { event ->
        eventListener(SignUpEvent.EditEmailAddress(event.value))
      }
    }
    SmallText {
      Text(emailAddressCaption)
    }

    PrimaryButton(
      attrs = {
        style {
          marginTop(24.px)
          marginBottom(24.px)
        }
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

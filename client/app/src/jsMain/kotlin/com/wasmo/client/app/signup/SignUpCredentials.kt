package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.ChildStyle
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpCredentials(childStyle: ChildStyle) {
  FormScreen(
    childStyle = childStyle,
  ) {
    SignUpToolbar(
      childStyle = ChildStyle {},
    )
    SignUpSegmentedProgressBar(
      stepsCompleted = 2,
      stepCount = 5,
    )
    TextField(
      childStyle = ChildStyle {},
      label = "Email Address",
      value = "jesse@swank.ca",
    )
    TextField(
      childStyle = ChildStyle {},
      label = "Passkey",
      value = "",
    )
    P {
      Text("You can sign later with either the passkey or the email. ")
      A(
        href = "https://www.wired.com/story/what-is-a-passkey-and-how-to-use-them/",
        attrs = {
          target(ATarget.Blank)
        },
      ) {
        Text("Learn about passkeys.")
      }
    }
    PrimaryButton(
      childStyle = ChildStyle {},
      label = "Create Account",
    )
  }
}

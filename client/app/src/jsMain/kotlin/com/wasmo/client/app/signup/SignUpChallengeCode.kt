package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SecondaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.ChildStyle
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpChallengeCode(childStyle: ChildStyle) {
  FormScreen(
    childStyle = childStyle,
  ) {
    SignUpToolbar(
      childStyle = ChildStyle {},
    )
    SignUpSegmentedProgressBar(
      stepsCompleted = 5,
      stepCount = 5,
    )
    P {
      Text("Enter the code we sent to jesse@swank.ca.")
    }
    TextField(
      childStyle = ChildStyle {},
      value = "1 2 3 4 5 6",
    )
    PrimaryButton(
      childStyle = ChildStyle {},
      label = "Finish",
    )
    SecondaryButton(
      childStyle = ChildStyle { },
      label = "Change Email",
    )
    SecondaryButton(
      childStyle = ChildStyle { },
      label = "Resend Code",
    )
  }
}

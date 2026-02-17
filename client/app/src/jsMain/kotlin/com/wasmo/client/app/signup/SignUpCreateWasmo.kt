package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.ChildStyle
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpCreateWasmo(childStyle: ChildStyle) {
  FormScreen(
    childStyle = childStyle,
  ) {
    SignUpToolbar(
      childStyle = ChildStyle {},
    )
    SignUpSegmentedProgressBar(
      stepsCompleted = 4,
      stepCount = 5,
    )
    P {
      Text("Name your Wasmo.")
    }
    TextField(
      childStyle = ChildStyle {},
      value = "jesse99",
    )
    P {
      Text("Names may use lowercase a-z characters and 0-9 numbers. No spaces or punctuation!")
    }
    PrimaryButton(
      childStyle = ChildStyle {},
      label = "Create Computer",
    )
  }
}

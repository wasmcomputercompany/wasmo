package com.wasmo.client.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

@Composable
fun SignUpIntro(childStyle: ChildStyle) {
  FormScreen(
    childStyle = childStyle,
  ) {
    H1 {
      Text("Sign Up")
    }
    SegmentedProgressBar(
      childStyle = ChildStyle {
      },
      stepsCompleted = 1,
      stepCount = 5,
      minGap = 8.px,
      height = 12.px,
    )
    P {
      Text("Wasmo is currently available in Canada.")
    }
    P {
      Text("To get started, you'll need:")
    }
    Ul {
      Li {
        Text("An email address")
      }
      Li {
        Text("A Canadian credit card")
      }
      Li {
        Text($$"$10")
      }
    }
    Input(
      type = InputType.Button,
      attrs = {
        classes("Primary")
        value("I’m ready, let’s go")
      },
    )
    Input(
      type = InputType.Button,
      attrs = {
        style {
          marginTop(12.px)
        }
        classes("Secondary")
        value("Other countries")
      },
    )
    Input(
      type = InputType.Button,
      attrs = {
        classes("Secondary")
        value("Questions")
      },
    )
  }
}

package com.wasmo.client.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.compose.ChildStyle
import com.wasmo.compose.SegmentedProgressBar
import com.wasmo.compose.Toolbar
import com.wasmo.compose.ToolbarTitle
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
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
    var stepsCompleted by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
      var i = 1
      while (true) {
        stepsCompleted = (i++ % 6)
        delay(1_000)
      }
    }

    Toolbar(
      childStyle = ChildStyle {
        marginBottom(8.px)
      },
      title = {
        ToolbarTitle(
          childStyle = ChildStyle {
          },
        ) {
          Text("Sign Up")
        }
      },
    )
    SegmentedProgressBar(
      childStyle = ChildStyle {
        marginBottom(8.px)
        marginTop(8.px)
      },
      stepsCompleted = stepsCompleted,
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

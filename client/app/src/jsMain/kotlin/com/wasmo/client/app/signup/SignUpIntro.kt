package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SecondaryButton
import com.wasmo.compose.ChildStyle
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
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

    SignUpToolbar(
      childStyle = ChildStyle { },
    )
    SignUpSegmentedProgressBar(
      stepsCompleted = stepsCompleted,
      stepCount = 5,
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
    PrimaryButton(
      childStyle = ChildStyle {},
      label = "I’m ready, let’s go",
    )
    SecondaryButton(
      childStyle = ChildStyle {
        marginTop(12.px)
      },
      label = "Other countries",
    )
    SecondaryButton(
      childStyle = ChildStyle {},
      label = "Questions",
    )
  }
}

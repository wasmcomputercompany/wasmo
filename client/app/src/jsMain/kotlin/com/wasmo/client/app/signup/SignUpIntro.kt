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
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.w3c.dom.HTMLDivElement

@Composable
fun SignUpIntro(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpIntroEvent) -> Unit,
) {
  FormScreen(
    attrs = attrs,
  ) {
    var stepsCompleted by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
      var i = 1
      while (true) {
        stepsCompleted = (i++ % 6)
        delay(1_000)
      }
    }

    SignUpToolbar()
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
      attrs = {
        style {
          marginTop(24.px)
          marginBottom(24.px)
        }
        value("I’m ready, let’s go")
        onClick {
          eventListener(
            SignUpIntroEvent.Proceed,
          )
        }
      },
    )
    SecondaryButton(
      attrs = {
        style {
          marginTop(12.px)
        }
        value("Other countries")
        onClick {
          eventListener(
            SignUpIntroEvent.OtherCountries,
          )
        }
      },
    )
    SecondaryButton(
      attrs = {
        value("Questions")
        onClick {
          eventListener(
            SignUpIntroEvent.Questions,
          )
        }
      },
    )
  }
}

sealed interface SignUpIntroEvent {
  object Proceed : SignUpIntroEvent
  object OtherCountries : SignUpIntroEvent
  object Questions : SignUpIntroEvent
}

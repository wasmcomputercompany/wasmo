package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.SecondaryButton
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

@Composable
fun SignUpIntro(
  eventListener: (SignUpIntroEvent) -> Unit,
) {
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
      onClick {
        eventListener(
          SignUpIntroEvent.Proceed,
        )
      }
    },
  ) {
    Text("I’m ready, let’s go")
  }
  SecondaryButton(
    attrs = {
      style {
        marginTop(12.px)
      }
      onClick {
        eventListener(
          SignUpIntroEvent.OtherCountries,
        )
      }
    },
  ) {
    Text("Other countries")
  }
  SecondaryButton(
    attrs = {
      onClick {
        eventListener(
          SignUpIntroEvent.Questions,
        )
      }
    },
  ) {
    Text("Questions")
  }
}

sealed interface SignUpIntroEvent {
  object Proceed : SignUpIntroEvent
  object OtherCountries : SignUpIntroEvent
  object Questions : SignUpIntroEvent
}

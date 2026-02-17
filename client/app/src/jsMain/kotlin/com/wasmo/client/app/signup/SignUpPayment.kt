package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.ChildStyle
import com.wasmo.compose.SegmentedProgressBar
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun SignUpPayment(childStyle: ChildStyle) {
  FormScreen(
    childStyle = childStyle,
  ) {
    SignUpToolbar(
      childStyle = ChildStyle {},
    )
    SignUpSegmentedProgressBar(
      stepsCompleted = 3,
      stepCount = 5,
    )
    P {
      Text("Wasmo.com is  pay as you go.")
    }
    P {
      Text($$"We’ll charge you $10 now to fund your account.")
    }
    P {
      Text($$"We’ll charge you again $10 (or less) each month to top it up. We’ll send an email 24 hours before we charge your card with the amount of the charge and a link to cancel.")
    }
    TextField(
      childStyle = ChildStyle {},
      label = "Full Name",
      value = "Jesse Wilson",
    )
    TextField(
      childStyle = ChildStyle {},
      label = "Card Number",
      value = "1111 2222 3333 4444",
    )
    TextField(
      childStyle = ChildStyle {},
      label = "MM/DD",
      value = "12/31",
    )
    TextField(
      childStyle = ChildStyle {},
      label = "CVV",
      value = "127",
    )
    TextField(
      childStyle = ChildStyle {},
      label = "Postal Code",
      value = "A1A 1A1",
    )
    PrimaryButton(
      childStyle = ChildStyle {},
      label = "Subscribe",
    )
  }
}

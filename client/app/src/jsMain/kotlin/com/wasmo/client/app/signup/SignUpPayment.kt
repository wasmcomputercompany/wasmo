package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import com.wasmo.compose.ChildStyle
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
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
      childStyle = ChildStyle {
        marginTop(24.px)
      },
      label = "Full Name",
      value = "Jesse Wilson",
    )
    TextField(
      childStyle = ChildStyle {},
      label = "Card Number",
      value = "1111 2222 3333 4444",
    )
    Div(
      attrs = {
        style {
          display(DisplayStyle.Flex)
          flexDirection(FlexDirection.Row)
        }
      },
    ) {
      TextField(
        childStyle = ChildStyle {
          marginRight(16.px)
          flex(100, 100, 0.px)
        },
        label = "MM/DD",
        value = "12/31",
      )
      TextField(
        childStyle = ChildStyle {
          marginRight(16.px)
          flex(100, 100, 0.px)
        },
        label = "CVV",
        value = "127",
      )
    }
    TextField(
      childStyle = ChildStyle {},
      label = "Postal Code",
      value = "A1A 1A1",
    )
    PrimaryButton(
      childStyle = ChildStyle {
        marginTop(24.px)
        marginBottom(24.px)
      },
      label = "Subscribe",
    )
  }
}

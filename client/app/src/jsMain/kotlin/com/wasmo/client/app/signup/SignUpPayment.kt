package com.wasmo.client.app.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.client.app.FormScreen
import com.wasmo.client.app.PrimaryButton
import com.wasmo.client.app.TextField
import org.jetbrains.compose.web.attributes.AttrsScope
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
import org.w3c.dom.HTMLDivElement

@Composable
fun SignUpPayment(
  attrs: AttrsScope<HTMLDivElement>.() -> Unit = {},
  eventListener: (SignUpPaymentEvent) -> Unit,
) {
  var fullNameState by remember { mutableStateOf("Jesse Wilson") }
  var cardNumberState by remember { mutableStateOf("1111 2222 3333 4444") }
  var mmddState by remember { mutableStateOf("12/31") }
  var cvvState by remember { mutableStateOf("123") }
  var postalCodeState by remember { mutableStateOf("A1A 1A1") }

  FormScreen(
    attrs = attrs,
  ) {
    SignUpToolbar()
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
      attrs = {
        style {
          marginTop(24.px)
        }
      },
      label = "Full Name",
      inputAttrs = {
        value(fullNameState)
        onInput { event ->
          fullNameState = event.value
        }
      },
    )
    TextField(
      label = "Card Number",
      inputAttrs = {
        value(cardNumberState)
        onInput { event ->
          cardNumberState = event.value
        }
      },
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
        attrs = {
          style {
            marginRight(16.px)
            flex(100, 100, 0.px)

          }
        },
        label = "MM/DD",
        inputAttrs = {
          value(mmddState)
          onInput { event ->
            mmddState = event.value
          }
        },
      )
      TextField(
        attrs = {
          style {
            marginRight(16.px)
            flex(100, 100, 0.px)
          }
        },
        label = "CVV",
      ) {
        value(cvvState)
        onInput { event ->
          cvvState = event.value
        }
      }
    }
    TextField(
      label = "Postal Code",
    ) {
      value(postalCodeState)
      onInput { event ->
        postalCodeState = event.value
      }
    }
    PrimaryButton(
      attrs = {
        style {
          marginTop(24.px)
          marginBottom(24.px)
        }
        value("Subscribe")
        onClick {
          eventListener(
            SignUpPaymentEvent.Subscribe(
              fullName = fullNameState,
              cardNumber = cardNumberState,
              mmdd = mmddState,
              cvv = cvvState,
              postalCode = postalCodeState,
            ),
          )
        }
      },
    )
  }
}

sealed interface SignUpPaymentEvent {
  data class Subscribe(
    val fullName: String,
    val cardNumber: String,
    val mmdd: String,
    val cvv: String,
    val postalCode: String,
  ) : SignUpPaymentEvent
}

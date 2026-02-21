package com.wasmo.client.app.stripe

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.Div

/**
 * Mounts the Stripe checkout UI in an iframe.
 */
@Composable
fun CheckoutScreen(checkoutSession: CheckoutSession) {
  Div(
    attrs = {
      style {
        display(DisplayStyle.Block)
      }
      ref { element ->
        checkoutSession.mount(element)
        onDispose {
          checkoutSession.unmount()
        }
      }
    },
  )
}

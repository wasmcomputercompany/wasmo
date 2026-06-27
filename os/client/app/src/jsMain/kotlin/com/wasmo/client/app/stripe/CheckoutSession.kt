package com.wasmo.client.app.stripe

import com.wasmo.api.payments.ComputerPaymentMethod
import com.wasmo.client.identifiers.ClientAppScope
import com.wasmo.compose.Checkout
import com.wasmo.compose.InitEmbeddedCheckoutOptions
import com.wasmo.compose.Stripe
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.js.Promise
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import org.w3c.dom.Element

/**
 * The client side of an embedded checkout session.
 *
 * https://docs.stripe.com/checkout/embedded/quickstart?lang=java
 */
class CheckoutSession private constructor(
  private val coroutineScope: CoroutineScope,
  private val deferredCheckout: Deferred<Checkout>,
) {
  fun mount(element: Element) {
    coroutineScope.launch {
      val checkout = deferredCheckout.await()
      checkout.mount(element)
    }
  }

  fun unmount() {
    coroutineScope.launch {
      val checkout = deferredCheckout.await()
      checkout.unmount()
    }
  }

  @Inject
  @SingleIn(ClientAppScope::class)
  class Factory(
    private val computerPaymentMethod: ComputerPaymentMethod,
  ) {
    fun create(
      coroutineScope: CoroutineScope,
      clientSecret: String,
    ): CheckoutSession {
      val deferredCheckout = when (computerPaymentMethod) {
        is ComputerPaymentMethod.FreeOnly -> {
          CompletableDeferred<Checkout>().apply {
            completeExceptionally(UnsupportedOperationException("checkout not supported"))
          }
        }
        is ComputerPaymentMethod.Stripe -> {
          val stripe = Stripe(computerPaymentMethod.stripePublishableKey.publishableKey)
          stripe.initEmbeddedCheckout(
            InitEmbeddedCheckoutOptions(
              fetchClientSecret = { Promise.resolve(clientSecret) },
            ),
          ).asDeferred()
        }
      }
      return CheckoutSession(
        deferredCheckout = deferredCheckout,
        coroutineScope = coroutineScope,
      )
    }
  }
}

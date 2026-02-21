package com.wasmo.client.app.stripe

import com.wasmo.api.RealWasmoApi
import com.wasmo.api.stripe.CreateCheckoutSessionRequest
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.compose.Checkout
import com.wasmo.compose.InitEmbeddedCheckoutOptions
import com.wasmo.compose.Stripe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
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

  class Factory(
    private val stripePublishableKey: StripePublishableKey,
    private val wasmoApi: RealWasmoApi,
  ) {
    fun create(coroutineScope: CoroutineScope): CheckoutSession {
      val stripe = Stripe(stripePublishableKey.publishableKey)
      val deferredCheckout = stripe.initEmbeddedCheckout(
        InitEmbeddedCheckoutOptions(
          fetchClientSecret = {
            coroutineScope.async {
              val response = wasmoApi.createCheckoutSession(CreateCheckoutSessionRequest)
              response.clientSecret
            }.asPromise()
          },
        ),
      ).asDeferred()

      return CheckoutSession(
        deferredCheckout = deferredCheckout,
        coroutineScope = coroutineScope,
      )
    }
  }
}

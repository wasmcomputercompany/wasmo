package com.wasmo.compose

import kotlin.js.Promise
import org.w3c.dom.Element

external class Stripe {
  constructor(publishableKey: String)

  /**
   * https://docs.stripe.com/js/embedded_checkout/init
   */
  fun initEmbeddedCheckout(options: InitEmbeddedCheckoutOptions): Promise<Checkout>
}

external class InitEmbeddedCheckoutOptions

fun InitEmbeddedCheckoutOptions(
  fetchClientSecret: (() -> Promise<String>),
  onComplete: (() -> Unit)? = null,
): InitEmbeddedCheckoutOptions {
  val result = js("{}")
  result.fetchClientSecret = fetchClientSecret
  if (onComplete != null) {
    result.onComplete = onComplete
  }
  return result
}

external class Checkout {
  fun mount(element: Element)
  fun unmount()
  fun destroy()
}

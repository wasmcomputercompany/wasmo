package com.wasmo.home

import com.wasmo.accounts.Client
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.deployment.Deployment

class HomePage(
  val deployment: Deployment,
  val stripePublishableKey: StripePublishableKey,
  val client: Client,
) {
  fun get(): AppPage {
    return AppPage(
      baseUrl = deployment.baseUrl,
      stripePublishableKey = stripePublishableKey,
    )
  }
}

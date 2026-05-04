package com.wasmo.stripe

import com.stripe.StripeClient
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.OsScope
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class StripeBindings {
  @Binds
  internal abstract fun bindPaymentsService(real: StripePaymentsService): PaymentsService

  companion object {
    @Provides
    @SingleIn(OsScope::class)
    fun provideStripeClient(
      stripeCredentials: StripeCredentials,
    ): StripeClient {
      return StripeClient.StripeClientBuilder()
        .setApiKey(stripeCredentials.secretKey)
        .build()
    }

    @Provides
    @SingleIn(OsScope::class)
    internal fun provideStripePaymentsService(
      deployment: Deployment,
      catalog: Catalog,
      stripeClient: StripeClient,
    ): StripePaymentsService = StripePaymentsService(
      deployment = deployment,
      sessionService = stripeClient.v1().checkout().sessions(),
      subscriptionService = stripeClient.v1().subscriptions(),
      catalog = catalog,
    )
  }
}

package com.wasmo.computers.paid

import com.wasmo.api.payments.ComputerPaymentMethod
import com.wasmo.api.payments.StripePublishableKey
import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class PaidComputersBindings {
  companion object {
    @Provides
    @IntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
        hostnamePatterns: HostnamePatterns,
    ): ActionRegistration =
      ActionRegistration.Rpc(
        host = hostnamePatterns.osHostname,
        path = "/create-computer-spec",
        action = PaidCreateComputerSpecRpc::class,
      )
    @Provides
    @SingleIn(OsScope::class)
    fun provideComputerPaymentConfig(stripePublishableKey: StripePublishableKey): ComputerPaymentMethod =
      ComputerPaymentMethod.Stripe(stripePublishableKey)
  }
}

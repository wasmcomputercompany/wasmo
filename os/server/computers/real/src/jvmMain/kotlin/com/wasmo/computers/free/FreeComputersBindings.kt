package com.wasmo.computers.free

import com.wasmo.api.payments.ComputerPaymentMethod
import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class FreeComputersBindings {
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
        action = FreeCreateComputerSpecRpc::class,
      )

    // TODO: Consider moving no-payment ComputerPaymentMethod and PaymentsService bindings to a
    // sibling of StripeBindings, since the paid versions are defined by StripeBindings.
    // Alternative, create a StripeComputersBindings module that depends on both.
    @Provides
    @SingleIn(OsScope::class)
    fun provideComputerPaymentConfig(): ComputerPaymentMethod = ComputerPaymentMethod.FreeOnly

    @Provides
    @SingleIn(OsScope::class)
    fun providePaymentsService(): PaymentsService = NoPaymentsService()
  }
}

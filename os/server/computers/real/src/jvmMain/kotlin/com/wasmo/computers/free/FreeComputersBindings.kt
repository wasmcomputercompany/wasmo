package com.wasmo.computers.free

import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
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
  }
}

package com.wasmo.usernames
import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class UsernameBindings {

  @Binds
  abstract fun bindUsernameLinker(real: RealUsernameLinker): UsernameLinker

  companion object {
    @Provides
    @ElementsIntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
      hostnamePatterns: HostnamePatterns,
    ): List<ActionRegistration> = listOf(
      ActionRegistration.Rpc(
        host = hostnamePatterns.osHostname,
        path = "/create-username",
        action = CreateUsernameRpc::class,
      ),
      ActionRegistration.Rpc(
        host = hostnamePatterns.osHostname,
        path = "/link-username",
        action = LinkUsernameRpc::class,
      ),
    )
  }
}

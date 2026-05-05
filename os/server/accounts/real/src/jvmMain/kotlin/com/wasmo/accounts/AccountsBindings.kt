package com.wasmo.accounts

import com.wasmo.accounts.invite.CreateInviteRpc
import com.wasmo.framework.ActionRegistration
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class AccountsBindings {
  @Binds
  abstract fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  companion object {
    @Provides
    @ElementsIntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
      hostnamePatterns: HostnamePatterns,
    ): List<ActionRegistration> = listOf(
      ActionRegistration.Http(
        host = hostnamePatterns.osHostname,
        path = "/sign-out",
        method = "GET",
        action = SignOutPage::class,
      ),

      ActionRegistration.Rpc(
        host = hostnamePatterns.osHostname,
        path = "/account-snapshot",
        action = AccountSnapshotRpc::class,
      ),

      ActionRegistration.Rpc(
        host = hostnamePatterns.osHostname,
        path = "/create-invite",
        action = CreateInviteRpc::class,
      ),

      ActionRegistration.Rpc(
        host = hostnamePatterns.osHostname,
        path = "/sign-out",
        action = SignOutRpc::class,
      ),
    )
  }
}

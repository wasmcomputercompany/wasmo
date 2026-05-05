package com.wasmo.accounts

import com.wasmo.accounts.invite.CreateInviteRpc
import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
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
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/sign-out",
        ),
        action = SignOutPage::class,
      ),

      ActionRegistration.Rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/account-snapshot",
        ),
        action = AccountSnapshotRpc::class,
      ),

      ActionRegistration.Rpc<CreateInviteRequest, CreateInviteResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/create-invite",
        ),
        action = CreateInviteRpc::class,
      ),

      ActionRegistration.Rpc<SignOutRequest, SignOutResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/sign-out",
        ),
        action = SignOutRpc::class,
      ),
    )
  }
}

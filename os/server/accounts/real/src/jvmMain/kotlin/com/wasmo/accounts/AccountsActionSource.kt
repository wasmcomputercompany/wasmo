package com.wasmo.accounts

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
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object AccountsActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    accountsActionsFactory: AccountsActions.Factory,
    hostnamePatterns: HostnamePatterns,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Http(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/sign-out",
      ),
    ) { userAgent, _, _ ->
      val action = accountsActionsFactory.create(userAgent).signOutPage
      action.get()
    },

    ActionRegistration.Rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/account-snapshot",
      ),
      action = { userAgent, request, _ ->
        val action = accountsActionsFactory.create(userAgent).accountSnapshotRpc
        action.get(request)
      },
    ),

    ActionRegistration.Rpc<CreateInviteRequest, CreateInviteResponse>(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/create-invite",
      ),
    ) { userAgent, request, _ ->
      val action = accountsActionsFactory.create(userAgent).createInviteRpc
      action.create(request)
    },

    ActionRegistration.Rpc<SignOutRequest, SignOutResponse>(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/sign-out",
      ),
    ) { userAgent, request, _ ->
      val action = accountsActionsFactory.create(userAgent).signOutRpc
      action.signOut(request)
    },
  )
}

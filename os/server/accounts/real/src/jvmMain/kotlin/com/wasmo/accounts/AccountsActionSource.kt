package com.wasmo.accounts

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.rpc
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class AccountsActionSource(
  private val accountsActionsFactory: AccountsActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: Binder)
  override fun bindActions() {
    binder.httpAction(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/sign-out",
      ),
    ) { userAgent, _, _ ->
      val action = accountsActionsFactory.create(userAgent).signOutPage
      action.get()
    }

    binder.rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/account-snapshot",
      ),
    ) { userAgent, request, _ ->
      val action = accountsActionsFactory.create(userAgent).accountSnapshotRpc
      action.get(request)
    }

    binder.rpc<CreateInviteRequest, CreateInviteResponse>(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/create-invite",
      ),
    ) { userAgent, request, _ ->
      val action = accountsActionsFactory.create(userAgent).createInviteRpc
      action.create(request)
    }

    binder.rpc<SignOutRequest, SignOutResponse>(
      pattern = HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/sign-out",
      ),
    ) { userAgent, request, _ ->
      val action = accountsActionsFactory.create(userAgent).signOutRpc
      action.signOut(request)
    }
  }
}

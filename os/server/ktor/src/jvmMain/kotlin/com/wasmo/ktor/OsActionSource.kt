package com.wasmo.ktor

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.UserAgent
import com.wasmo.framework.rpc
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class OsActionSource(
  private val callGraphFactory: NewCallGraphFactory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  private fun callGraph(userAgent: UserAgent) = callGraphFactory.create(userAgent)

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.osHostname) {
      route("/sign-out") {
        httpAction { userAgent, _, _ ->
          val callGraph = callGraph(userAgent)
          callGraph.signOutPage.get()
        }
      }

      rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
        path = "/account-snapshot",
      ) { userAgent, request, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.accountSnapshotRpc.get(request)
      }

      rpc<CreateInviteRequest, CreateInviteResponse>(
        path = "/create-invite",
      ) { userAgent, request, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.createInviteRpc.create(request)
      }

      rpc<SignOutRequest, SignOutResponse>(
        path = "/sign-out",
      ) { userAgent, request, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.signOutRpc.signOut(request)
      }
    }
  }
}

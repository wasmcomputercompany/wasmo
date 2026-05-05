package com.wasmo.ktor

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.UserAgent
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.redirect
import com.wasmo.framework.rpc
import com.wasmo.framework.toHttpUrl
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class OsActionSource(
  private val callGraphFactory: NewCallGraphFactory,
  private val hostnamePatterns: HostnamePatterns,
  deployment: Deployment,
) : ActionSource {
  override val order: Int
    get() = 5

  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  private fun callGraph(userAgent: UserAgent) = callGraphFactory.create(userAgent)

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.osHostname) {
      val osPagePaths = listOf(
        "/",
        "/build-yours",
        "/invite/{code}",
        "/sign-up",
      )
      for (path in osPagePaths) {
        route(path, "GET") {
          httpAction { userAgent, url, _ ->
            val callGraph = callGraph(userAgent)
            callGraph.osPage.get(url).response
          }
        }
      }

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

      rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
        path = "/create-computer-spec",
      ) { userAgent, request, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.createComputerSpecRpc.create(request)
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

      staticResources("/", "static")
    }

    binder.routeAll {
      httpAction { _, url, _ ->
        when {
          url.topPrivateDomain != rootUrl.topPrivateDomain || url.subdomain != rootUrl.subdomain ->
            redirect(rootUrl.toHttpUrl())

          else -> NotFoundUserException().asResponse()
        }
      }
    }
  }
}

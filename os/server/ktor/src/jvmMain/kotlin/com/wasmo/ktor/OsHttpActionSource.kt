package com.wasmo.ktor

import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.deployment.Deployment
import com.wasmo.framework.HttpActionBinder
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.UserAgent
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.redirect
import com.wasmo.framework.rpc
import com.wasmo.framework.toHttpUrl
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class OsHttpActionSource(
  private val callGraphStarter: CallGraphStarter,
  deployment: Deployment,
) : HttpActionSource {
  override val order: Int
    get() = 2

  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  private fun callGraph(userAgent: UserAgent) =
    callGraphStarter.start(userAgent)

  context(binder: HttpActionBinder)
  override fun bindActions() {
    binder.host(rootUrl.topPrivateDomain) {
      createPages()
      createRpcs()
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

  context(binder: HttpActionBinder)
  private fun createPages() {
    val osPagePaths = listOf(
      "/",
      "/build-yours",
      "/invite/{code}",
      "/sign-up",
    )
    for (path in osPagePaths) {
      binder.route(path, "GET") {
        httpAction { userAgent, url, _ ->
          val callGraph = callGraph(userAgent)
          callGraph.osPage.get(url).response
        }
      }
    }

    binder.route("/after-checkout/{checkoutSessionId}") {
      httpAction { userAgent, url, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.afterCheckoutPage.get(url.path[1])
      }
    }

    binder.route("/sign-out") {
      httpAction { userAgent, _, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.signOutPage.get()
      }
    }
  }

  context(binder: HttpActionBinder)
  private fun createRpcs() {
    binder.rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
      path = "/account-snapshot",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.accountSnapshotRpc.get(request)
    }

    binder.rpc<AuthenticatePasskeyRequest, AuthenticatePasskeyResponse>(
      path = "/authenticate-passkey",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.authenticatePasskeyRpc.authenticate(request)
    }

    binder.rpc<ConfirmEmailAddressRequest, ConfirmEmailAddressResponse>(
      path = "/confirm-email-address",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.confirmEmailAddressRpc.confirm(request)
    }

    binder.rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
      path = "/create-computer-spec",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.createComputerSpecRpc.create(request)
    }

    binder.rpc<CreateInviteRequest, CreateInviteResponse>(
      path = "/create-invite",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.createInviteRpc.create(request)
    }

    binder.rpc<LinkEmailAddressRequest, LinkEmailAddressResponse>(
      path = "/link-email-address",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.linkEmailAddressRpc.link(request)
    }

    binder.rpc<RegisterPasskeyRequest, RegisterPasskeyResponse>(
      path = "/register-passkey",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.registerPasskeyRpc.register(request)
    }

    binder.rpc<SignOutRequest, SignOutResponse>(
      path = "/sign-out",
    ) { userAgent, request, _ ->
      val callGraph = callGraph(userAgent)
      callGraph.signOutRpc.signOut(request)
    }
  }
}


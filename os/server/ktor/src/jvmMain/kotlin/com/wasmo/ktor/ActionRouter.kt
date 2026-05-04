package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
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
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.api.SignOutRequest
import com.wasmo.api.SignOutResponse
import com.wasmo.api.routes.decodeUrl
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.deployment.Deployment
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.redirect
import com.wasmo.identifiers.AppSlugRegex
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSlugRegex
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.routing.routing

@Inject
@SingleIn(OsScope::class)
class ActionRouter(
  deployment: Deployment,
  private val application: Application,
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val callGraphFactory: CallGraph.Factory,
  private val httpActionBinderFactory: KtorHttpActionBinder.Factory,
) {
  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  fun createRoutes() {
    application.install(CallLogging)

    val suffixRegex = Regex.escape(".${rootUrl.topPrivateDomain}")
    val computerRegex = Regex("${ComputerSlugRegex.pattern}$suffixRegex")
    val appRegex = Regex("${AppSlugRegex.pattern}-${ComputerSlugRegex.pattern}$suffixRegex")

    application.routing {
      context(httpActionBinderFactory.create(this)) {
        bindRoutes(computerRegex, appRegex)
      }
    }
  }

  context(binder: HttpActionBinder)
  private fun bindRoutes(
    computerRegex: Regex,
    appRegex: Regex,
  ) {
    binder.host(computerRegex) {
      createComputerRoutes()

      // TODO: change the web app to always fetch static resources from the root URL.
      staticResources("/", "static")
    }
    binder.host(appRegex) {
      createAppRoutes()
    }
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
  private fun createComputerRoutes() {
    binder.route("/") {
      httpAction { userAgent, url, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.osPage.get(url).response
      }
    }

    binder.rpc<InstallAppRequest, InstallAppResponse>(
      path = "/install-app",
    ) { userAgent, request, wasmoUrl ->
      val callGraph = callGraph(userAgent)
      callGraph.installAppRpc.install(
        computerSlug = ComputerSlug(wasmoUrl.subdomain ?: throw NotFoundUserException()),
        request = request,
      )
    }
  }

  context(binder: HttpActionBinder)
  private fun createAppRoutes() {
    binder.routeAll {
      httpAction { userAgent, _, request ->
        val callGraph = callGraph(userAgent)
        callGraph.callAppAction.call(request)
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

  private fun callGraph(userAgent: ClientAuthenticator.UserAgent): CallGraph {
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    clientAuthenticator.updateSessionCookie()
    return callGraphFactory.create(clientAuthenticator.get())
  }
}

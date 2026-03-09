package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AccountSnapshotResponse
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSlugRegex
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
import com.wasmo.api.routes.Url
import com.wasmo.api.routes.decodeUrl
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.deployment.Deployment
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.UserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.redirect
import com.wasmo.identifiers.AppSlugRegex
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.host
import io.ktor.server.request.path
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext as KtorRoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.host
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.serializer

@Inject
@SingleIn(AppScope::class)
class ActionRouter(
  deployment: Deployment,
  private val application: Application,
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val callGraphFactory: CallGraph.Factory,
) {
  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  fun createRoutes() {
    application.install(CallLogging)

    val suffixRegex = Regex.escape(".${rootUrl.topPrivateDomain}")
    val computerRegex = Regex("${ComputerSlugRegex.pattern}$suffixRegex")
    val appRegex = Regex("${AppSlugRegex.pattern}-${ComputerSlugRegex.pattern}$suffixRegex")

    application.routing {
      host(computerRegex) {
        createComputerRoutes()
      }
      host(appRegex) {
        createAppRoutes()
      }
      host(rootUrl.topPrivateDomain) {
        createPages()
        createRpcs()
      }
      handle {
        val response = when {
          call.request.host() != rootUrl.topPrivateDomain -> redirect(rootUrl.toHttpUrl())
          else -> NotFoundUserException().asResponse()
        }
        call.respond(response)
      }
    }

    application.routing {
      staticResources("/", "static")
    }
  }

  private fun Route.createComputerRoutes() {
    get("/") { callGraph, url, _ ->
      callGraph.hostPageAction.get(url).response
    }
  }

  // TODO: replace this with something that invokes app code, or serves app static files.
  private fun Route.createAppRoutes() {
    get("/") { callGraph, url, _ ->
      callGraph.hostPageAction.get(url).response
    }
  }

  private fun Route.createPages() {
    for (path in listOf("/", "/build-yours", "/computers", "/teaser", "/invite/{code}")) {
      get(path) { callGraph, url, _ ->
        callGraph.hostPageAction.get(url).response
      }
    }

    get("/after-checkout/{checkoutSessionId}") { callGraph, _, call ->
      callGraph.afterCheckoutAction.get(call.pathParameters["checkoutSessionId"]!!)
    }
  }

  private fun Route.createRpcs() {
    rpc<CreateInviteRequest, CreateInviteResponse>(
      path = "/create-invite",
    ) { callGraph, request, _ ->
      callGraph.createInviteAction.create(request)
    }

    rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
      path = "/account-snapshot",
    ) { callGraph, request, _ ->
      callGraph.accountSnapshotAction.get(request)
    }

    rpc<RegisterPasskeyRequest, RegisterPasskeyResponse>(
      path = "/register-passkey",
    ) { callGraph, request, _ ->
      callGraph.registerPasskeyAction.register(request)
    }

    rpc<AuthenticatePasskeyRequest, AuthenticatePasskeyResponse>(
      path = "/authenticate-passkey",
    ) { callGraph, request, _ ->
      callGraph.authenticatePasskeyAction.authenticate(request)
    }

    rpc<InstallAppRequest, InstallAppResponse>(
      path = "/computers/{computer}/install-app",
    ) { callGraph, request, call ->
      callGraph.installAppAction.install(
        computerSlug = ComputerSlug(call.pathParameters["computer"]!!),
        request = request,
      )
    }

    rpc<LinkEmailAddressRequest, LinkEmailAddressResponse>(
      path = "/link-email-address",
    ) { callGraph, request, _ ->
      callGraph.linkEmailAddressAction.link(request)
    }

    rpc<ConfirmEmailAddressRequest, ConfirmEmailAddressResponse>(
      path = "/confirm-email-address",
    ) { callGraph, request, _ ->
      callGraph.confirmEmailAddressAction.confirm(request)
    }

    rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
      path = "/create-computer-spec",
    ) { callGraph, request, _ ->
      callGraph.createComputerSpecAction.create(request)
    }
  }

  private inline fun Route.get(
    path: String,
    crossinline action: suspend (CallGraph, Url, RoutingCall) -> Response<ResponseBody>,
  ) {
    get(path) {
      val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
      val response = try {
        clientAuthenticator.updateSessionCookie()
        val callGraph = callGraphFactory.create(clientAuthenticator.get())
        val url = wasmoUrl(rootUrl)
        action(callGraph, url, call)
      } catch (e: UserException) {
        application.log.info("call failed", e)
        call.respond(e.asResponse())
        return@get
      }
      call.respond(response)
    }
  }

  private inline fun <reified R, reified S> Route.rpc(
    path: String,
    crossinline action: suspend (CallGraph, R, RoutingCall) -> Response<S>,
  ) {
    val requestAdapter = serializer<R>()
    val responseAdapter = serializer<S>()

    post(path) {
      val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
      val response = try {
        val request = requestAdapter.decode(call.request)
        val client = clientAuthenticator.get()
        val callGraph = callGraphFactory.create(client)
        action(callGraph, request, call)
      } catch (e: UserException) {
        application.log.info("call failed", e)
        call.respond(e.asResponse())
        return@post
      }
      call.respond(
        serializer = responseAdapter,
        response = response,
      )
    }
  }

  private fun KtorRoutingContext.wasmoUrl(rootUrl: Url): Url {
    val host = call.request.host()
    val dotIndex = host.length - rootUrl.topPrivateDomain.length - 1
    val subdomain = when {
      dotIndex >= 1 && host.endsWith(rootUrl.topPrivateDomain) -> host.take(dotIndex)
      else -> null
    }

    return rootUrl.copy(
      subdomain = subdomain,
      path = call.request.path().removePrefix("/").split("/"),
    )
  }
}

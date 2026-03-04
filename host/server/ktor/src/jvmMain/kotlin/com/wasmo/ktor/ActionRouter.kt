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
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
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
import com.wasmo.deployment.Deployment
import com.wasmo.framework.HttpException
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
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
import io.ktor.server.routing.Routing
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

    createComputerRoutes()
    createPages()
    createRpcs()

    application.routing {
      staticResources("/", "static")
    }
  }

  private fun createComputerRoutes() {
    val suffix = ".${rootUrl.topPrivateDomain}"
    val hostRegex = Regex("${ComputerSlugRegex.pattern}${Regex.escape(suffix)}")
    application.routing {
      host(hostRegex) {
        get("/") { callGraph, url, _ ->
          callGraph.hostPageAction.get(url).response
        }
      }
    }
  }

  private fun createPages() {
    application.routing {
      for (path in listOf("/", "/build-yours", "/computers", "/teaser", "/invite/{code}")) {
        get(path) { callGraph, url, _ ->
          callGraph.hostPageAction.get(url).response
        }
      }

      get("/after-checkout/{checkoutSessionId}") { callGraph, _, call ->
        callGraph.afterCheckoutAction.get(call.pathParameters["checkoutSessionId"]!!)
      }
    }
  }

  private fun createRpcs() {
    application.routing {
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

      rpc<CreateComputerRequest, CreateComputerResponse>(
        path = "/create-computer",
      ) { callGraph, request, _ ->
        callGraph.createComputerAction.create(request)
      }
    }
  }

  private inline fun Routing.get(
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
      } catch (e: HttpException) {
        application.log.info("call failed", e)
        call.respond(e.asResponse())
        return@get
      }
      call.respond(response)
    }
  }

  private inline fun <reified R, reified S> Routing.rpc(
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
      } catch (e: HttpException) {
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

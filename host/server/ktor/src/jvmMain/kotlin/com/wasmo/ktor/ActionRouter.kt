package com.wasmo.ktor

import com.wasmo.accounts.Client
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
  private val deployment: Deployment,
  private val application: Application,
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val clientGraphFactory: ClientGraph.Factory,
) {
  fun accountSnapshotAction(client: Client) =
    clientGraphFactory.create(client).accountSnapshotAction

  fun createInviteAction(client: Client) =
    clientGraphFactory.create(client).createInviteAction

  fun registerPasskeyAction(client: Client) =
    clientGraphFactory.create(client).registerPasskeyAction

  fun authenticatePasskeyAction(client: Client) =
    clientGraphFactory.create(client).authenticatePasskeyAction

  fun linkEmailAddressAction(client: Client) =
    clientGraphFactory.create(client).linkEmailAddressAction

  fun confirmEmailAddressAction(client: Client) =
    clientGraphFactory.create(client).confirmEmailAddressAction

  fun createComputerAction(client: Client) =
    clientGraphFactory.create(client).createComputerAction

  fun installAppAction(client: Client) =
    clientGraphFactory.create(client).installAppAction

  fun hostPageAction(client: Client) =
    clientGraphFactory.create(client).hostPageAction

  fun afterCheckoutAction(client: Client) =
    clientGraphFactory.create(client).afterCheckoutAction

  fun createRoutes() {
    application.install(CallLogging)

    val rootUrl = deployment.baseUrl.toString().decodeUrl()

    createComputerRoutes(rootUrl)
    createPages(rootUrl)
    createRpcs()

    application.routing {
      staticResources("/", "static")
    }
  }

  private fun createComputerRoutes(rootUrl: Url) {
    val suffix = ".${rootUrl.topPrivateDomain}"
    val hostRegex = Regex("${ComputerSlugRegex.pattern}${Regex.escape(suffix)}")
    application.routing {
      host(hostRegex) {
        get("/") {
          val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
          clientAuthenticator.updateSessionCookie()
          val action = hostPageAction(clientAuthenticator.get())
          val page = action.get(wasmoUrl(rootUrl))
          call.respond(page.response)
        }
      }
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

  private fun createPages(rootUrl: Url) {
    application.routing {
      for (path in listOf("/", "/build-yours", "/computers", "/teaser", "/invite/{code}")) {
        get(path) {
          val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
          clientAuthenticator.updateSessionCookie()
          val action = hostPageAction(clientAuthenticator.get())
          val page = action.get(wasmoUrl(rootUrl))
          call.respond(page.response)
        }
      }

      get("/after-checkout/{checkoutSessionId}") {
        val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
        clientAuthenticator.updateSessionCookie()
        val action = afterCheckoutAction(clientAuthenticator.get())
        val page = action.get(call.pathParameters["checkoutSessionId"]!!)
        call.respond(page)
      }
    }
  }

  private fun createRpcs() {
    application.routing {
      rpc<CreateInviteRequest, CreateInviteResponse>(
        path = "/create-invite",
      ) { client, request, _ ->
        val action = createInviteAction(client)
        action.create(request)
      }

      rpc<AccountSnapshotRequest, AccountSnapshotResponse>(
        path = "/account-snapshot",
      ) { client, request, _ ->
        val action = accountSnapshotAction(client)
        action.get(request)
      }

      rpc<RegisterPasskeyRequest, RegisterPasskeyResponse>(
        path = "/register-passkey",
      ) { client, request, _ ->
        val action = registerPasskeyAction(client)
        action.register(request)
      }

      rpc<AuthenticatePasskeyRequest, AuthenticatePasskeyResponse>(
        path = "/authenticate-passkey",
      ) { client, request, _ ->
        val action = authenticatePasskeyAction(client)
        action.authenticate(request)
      }

      rpc<InstallAppRequest, InstallAppResponse>(
        path = "/computers/{computer}/install-app",
      ) { client, request, call ->
        val action = installAppAction(client)
        action.install(
          computerSlug = ComputerSlug(call.pathParameters["computer"]!!),
          request = request,
        )
      }

      rpc<LinkEmailAddressRequest, LinkEmailAddressResponse>(
        path = "/link-email-address",
      ) { client, request, _ ->
        val action = linkEmailAddressAction(client)
        action.link(request)
      }

      rpc<ConfirmEmailAddressRequest, ConfirmEmailAddressResponse>(
        path = "/confirm-email-address",
      ) { client, request, _ ->
        val action = confirmEmailAddressAction(client)
        action.confirm(request)
      }

      rpc<CreateComputerRequest, CreateComputerResponse>(
        path = "/create-computer",
      ) { client, request, _ ->
        val action = createComputerAction(client)
        action.create(request)
      }
    }
  }

  private inline fun <reified R, reified S> Routing.rpc(
    path: String,
    crossinline action: suspend (Client, R, RoutingCall) -> Response<S>,
  ) {
    val requestAdapter = serializer<R>()
    val responseAdapter = serializer<S>()

    post(path) {
      val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
      val response = try {
        val request = requestAdapter.decode(call.request)
        val client = clientAuthenticator.get()
        action(client, request, call)
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
}

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
import com.wasmo.api.WasmoJson
import com.wasmo.api.routes.Url
import com.wasmo.api.routes.decodeUrl
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Header
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.UserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.redirect
import com.wasmo.identifiers.AppSlugRegex
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSlugRegex
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.host
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext as KtorRoutingContext
import io.ktor.server.routing.RoutingRequest
import io.ktor.server.routing.host
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.url
import io.ktor.utils.io.asSource
import kotlinx.io.okio.asOkioSource
import kotlinx.serialization.serializer
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.buffer

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

        // TODO: change the web app to always fetch static resources from the root URL.
        staticResources("/", "static")
      }
      host(appRegex) {
        createAppRoutes()
      }
      host(rootUrl.topPrivateDomain) {
        createPages()
        createRpcs()
        staticResources("/", "static")
      }
      routeAll {
        handle {
          val response = when {
            call.request.host() != rootUrl.topPrivateDomain -> redirect(rootUrl.toHttpUrl())
            else -> NotFoundUserException().asResponse()
          }
          call.respond(response)
        }
      }
    }
  }

  private fun Route.createComputerRoutes() {
    route("/", HttpMethod.Get) {
      handle { callGraph, url, _ ->
        callGraph.hostPageAction.get(url).response
      }
    }
  }

  private fun Route.createAppRoutes() {
    routeAll {
      handle { callGraph, _, call ->
        callGraph.callAppAction.call(
          request = call.request.toRequest(),
        )
      }
    }
  }

  private fun Route.createPages() {
    for (path in listOf("/", "/build-yours", "/computers", "/teaser", "/invite/{code}")) {
      route(path, HttpMethod.Get) {
        handle { callGraph, url, _ ->
          callGraph.hostPageAction.get(url).response
        }
      }
    }

    route("/after-checkout/{checkoutSessionId}", HttpMethod.Get) {
      handle { callGraph, _, call ->
        callGraph.afterCheckoutAction.get(call.pathParameters["checkoutSessionId"]!!)
      }
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

  private inline fun <reified R, reified S> Route.rpc(
    path: String,
    crossinline action: suspend (CallGraph, R, RoutingCall) -> Response<S>,
  ) {
    val requestAdapter = serializer<R>()
    val responseAdapter = serializer<S>()

    route(path, HttpMethod.Post) {
      handle { callGraph, _, call ->
        val request = requestAdapter.decode(call.request)
        val response = action(callGraph, request, call)
        Response(
          status = response.status,
          headers = response.headers,
          contentType = response.contentType,
          body = ResponseBody { sink ->
            sink.writeUtf8(WasmoJson.encodeToString(responseAdapter, response.body))
          },
        )
      }
    }
  }

  private inline fun Route.handle(
    crossinline action: suspend (CallGraph, Url, RoutingCall) -> Response<ResponseBody>,
  ) {
    handle {
      val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
      val response = try {
        clientAuthenticator.updateSessionCookie()
        val callGraph = callGraphFactory.create(clientAuthenticator.get())
        action(callGraph, wasmoUrl(), call)
      } catch (e: UserException) {
        application.log.info("call failed", e)
        call.respond(e.asResponse())
        return@handle
      }
      call.respond(response)
    }
  }

  private fun KtorRoutingContext.wasmoUrl(): Url {
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

  private fun RoutingRequest.toRequest() = Request(
    method = call.request.httpMethod.value,
    url = call.url().toHttpUrl(),
    headers = buildList {
      for ((name, values) in headers.entries()) {
        for (value in values) {
          add(Header(name, value))
        }
      }
    },
    body = receiveChannel().asSource().asOkioSource().buffer().readByteString(),
  )

  private fun Route.routeAll(build: Route.() -> Unit) {
    route(Regex("/.*")) {
      build()
    }
  }
}

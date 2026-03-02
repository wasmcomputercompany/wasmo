package com.wasmo.ktor

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
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
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.Url
import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.computers.SubscriptionUpdater
import com.wasmo.deployment.Deployment
import com.wasmo.framework.HttpException
import com.wasmo.framework.Response
import com.wasmo.passkeys.PasskeyChecker
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.payments.PaymentsService
import com.wasmo.sendemail.SendEmailService
import com.wasmo.website.HostPageAction
import com.wasmo.website.ServerHostPage
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
import kotlin.time.Clock
import kotlinx.serialization.serializer

class ActionRouter(
  val clock: Clock,
  val deployment: Deployment,
  val application: Application,
  val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  val accountStoreFactory: AccountStore.Factory,
  val passkeyLinkerFactory: PasskeyLinker.Factory,
  val computerStore: ComputerStore,
  val sendEmailService: SendEmailService,
  val wasmoDbService: WasmoDbService,
  val serverHostPageFactory: ServerHostPage.Factory,
  val routingContext: RoutingContext,
  val routeCodec: RouteCodec,
  val inviteService: InviteService,
  val subscriptionUpdater: SubscriptionUpdater,
  val paymentsService: PaymentsService,
  val computerSpecStore: ComputerSpecStore,
) {
  private fun passkeyChecker(client: Client): PasskeyChecker = RealPasskeyChecker(
    challenger = client.challenger,
    deployment = deployment,
  )

  fun accountSnapshotAction(client: Client) = AccountSnapshotAction(
    accountStoreFactory = accountStoreFactory,
    client = client,
    wasmoDbService = wasmoDbService,
  )

  fun createInviteAction(client: Client) = CreateInviteAction(
    client = client,
    routeCodec = routeCodec,
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

  fun registerPasskeyAction(client: Client) = RegisterPasskeyAction(
    clock = clock,
    accountStoreFactory = accountStoreFactory,
    client = client,
    passkeyChecker = passkeyChecker(client),
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

  fun authenticatePasskeyAction(client: Client) = AuthenticatePasskeyAction(
    client = client,
    passkeyChecker = passkeyChecker(client),
    passkeyLinkerFactory = passkeyLinkerFactory,
    accountStoreFactory = accountStoreFactory,
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

  fun linkEmailAddressAction(client: Client) = LinkEmailAddressAction(
    deployment = deployment,
    sendEmailService = sendEmailService,
    client = client,
  )

  fun confirmEmailAddressAction(client: Client) = ConfirmEmailAddressAction(
    client = client,
  )

  fun createComputerAction(client: Client) = CreateComputerAction(
    paymentsService = paymentsService,
    client = client,
    wasmoDbService = wasmoDbService,
    computerSpecStore = computerSpecStore,
  )

  fun installAppAction(client: Client) = InstallAppAction(
    client = client,
    computerStore = computerStore,
    wasmoDbService = wasmoDbService,
  )

  fun hostPage(client: Client) = HostPageAction(
    client = client,
    deployment = deployment,
    accountStoreFactory = accountStoreFactory,
    hostPageFactory = serverHostPageFactory,
    wasmoDbService = wasmoDbService,
  )

  fun afterCheckoutAction(client: Client) = AfterCheckoutAction(
    paymentsService = paymentsService,
    routeCodec = routeCodec,
    client = client,
    subscriptionUpdater = subscriptionUpdater,
  )

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
    val suffix = ".${routingContext.root.topPrivateDomain}"
    val hostRegex = Regex("${ComputerSlugRegex.pattern}${Regex.escape(suffix)}")
    application.routing {
      host(hostRegex) {
        get("/") {
          val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
          clientAuthenticator.updateSessionCookie()
          val action = hostPage(clientAuthenticator.get())
          val page = action.get(wasmoUrl())
          call.respond(page.response)
        }
      }
    }
  }

  private fun KtorRoutingContext.wasmoUrl(): Url {
    val host = call.request.host()
    val dotIndex = host.length - routingContext.root.topPrivateDomain.length - 1
    val subdomain = when {
      dotIndex >= 1 && host.endsWith(routingContext.root.topPrivateDomain) -> host.take(dotIndex)
      else -> null
    }

    return routingContext.root.copy(
      subdomain = subdomain,
      path = call.request.path().removePrefix("/").split("/"),
    )
  }

  private fun createPages() {
    application.routing {
      for (path in listOf("/", "/build-yours", "/computers", "/teaser", "/invite/{code}")) {
        get(path) {
          val clientAuthenticator = clientAuthenticatorFactory.create(KtorUserAgent(this))
          clientAuthenticator.updateSessionCookie()
          val action = hostPage(clientAuthenticator.get())
          val page = action.get(wasmoUrl())
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

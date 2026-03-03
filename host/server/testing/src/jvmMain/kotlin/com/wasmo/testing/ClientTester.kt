package com.wasmo.testing

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.Client
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.InviteTicket
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.RealCallDataService
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.common.tokens.newToken
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.computers.SubscriptionUpdater
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.sendemail.SendEmailService
import com.wasmo.website.HostPageAction
import com.wasmo.website.RealServerHostPage
import com.wasmo.website.ServerHostPage
import kotlin.time.Clock

class ClientTester(
  private val clock: Clock,
  private val wasmoDbService: WasmoDbService,
  private val deployment: Deployment,
  private val sendEmailService: SendEmailService,
  private val clientAuthenticator: ClientAuthenticator,
  private val computerStore: ComputerStore,
  private val computerSpecStore: ComputerSpecStore,
  private val subscriptionUpdater: SubscriptionUpdater,
  private val stripePublishableKey: StripePublishableKey,
  val paymentsService: FakePaymentsService,
  val challenger: Challenger,
) {
  val authenticatorDatabase = RealAuthenticatorDatabase()

  val routeCodecFactory = object : RouteCodec.Factory {
    override fun create(routingContext: RoutingContext) = RealRouteCodec(routingContext)
  }

  val callDataServiceFactory = object : RealCallDataService.Factory {
    override fun create(client: Client) = RealCallDataService(
      deployment = deployment,
      authenticatorDatabase = authenticatorDatabase,
      routeCodecFactory = routeCodecFactory,
      wasmoDbService = wasmoDbService,
      client = client,
    )
  }

  val passkeyChecker = RealPasskeyChecker(
    challenger = challenger,
    deployment = deployment,
  )

  val passkeyLinkerFactory = object : PasskeyLinker.Factory {
    override fun create(client: Client) = PasskeyLinker(
      wasmoDbService = wasmoDbService,
      client = client,
    )
  }

  val inviteService = InviteService(
    clock = clock,
    wasmoDbService = wasmoDbService,
  )

  val hostPageFactory = object : RealServerHostPage.Factory {
    override fun create(
      routingContext: RoutingContext,
      accountSnapshot: AccountSnapshot,
      inviteTicket: InviteTicket?,
      computerSnapshot: ComputerSnapshot?,
      computerListSnapshot: ComputerListSnapshot?,
    ) = RealServerHostPage(
      deployment = deployment,
      stripePublishableKey = stripePublishableKey,
      routingContext = routingContext,
      accountSnapshot = accountSnapshot,
      inviteTicket = inviteTicket,
      computerSnapshot = computerSnapshot,
      computerListSnapshot = computerListSnapshot,
    )
  }

  fun routingContext() = RoutingContext(
    rootUrl = deployment.baseUrl.toString(),
    hasComputers = false,
    hasInvite = false,
    isAdmin = false,
  )

  fun routeCodec(): RouteCodec = routeCodecFactory.create(
    routingContext = routingContext(),
  )

  fun accountSnapshotAction() = AccountSnapshotAction(
    callDataService = callDataServiceFactory.create(clientAuthenticator.get()),
    wasmoDbService = wasmoDbService,
  )

  fun linkEmailAddressAction() = LinkEmailAddressAction(
    deployment = deployment,
    sendEmailService = sendEmailService,
    client = clientAuthenticator.get(),
  )

  fun confirmEmailAddressAction() = ConfirmEmailAddressAction(
    client = clientAuthenticator.get(),
  )

  fun registerPasskeyAction(): RegisterPasskeyAction {
    val client = clientAuthenticator.get()
    return RegisterPasskeyAction(
      clock = clock,
      callDataService = callDataServiceFactory.create(client),
      client = client,
      passkeyChecker = passkeyChecker,
      wasmoDbService = wasmoDbService,
      inviteService = inviteService,
    )
  }

  fun register(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ) = registerPasskeyAction().register(
    request = RegisterPasskeyRequest(
      registration = passkey.registration(
        challenge = challenger.create(),
        origin = deployment.baseUrl.toString(),
      ),
      inviteCode = inviteCode,
    ),
  )

  fun hostPageAction() = HostPageAction(
    callDataService = callDataServiceFactory.create(clientAuthenticator.get()),
    hostPageFactory = hostPageFactory,
    wasmoDbService = wasmoDbService,
  )

  fun hostPage(route: Route): ServerHostPage {
    val url = routeCodec().encode(route)
    return hostPageAction().get(url)
  }

  fun authenticatePasskeyAction(): AuthenticatePasskeyAction {
    val client = clientAuthenticator.get()
    return AuthenticatePasskeyAction(
      callDataService = callDataServiceFactory.create(client),
      client = client,
      passkeyChecker = passkeyChecker,
      passkeyLinker = passkeyLinkerFactory.create(client),
      wasmoDbService = wasmoDbService,
      inviteService = inviteService,
    )
  }

  fun createComputerAction() = CreateComputerAction(
    paymentsService = paymentsService,
    client = clientAuthenticator.get(),
    wasmoDbService = wasmoDbService,
    computerSpecStore = computerSpecStore,
  )

  fun afterCheckoutAction() = AfterCheckoutAction(
    paymentsService = paymentsService,
    subscriptionUpdater = subscriptionUpdater,
    callDataService = callDataServiceFactory.create(clientAuthenticator.get()),
    wasmoDbService = wasmoDbService,
  )

  fun createInviteAction(): CreateInviteAction {
    val client = clientAuthenticator.get()
    return CreateInviteAction(
      client = clientAuthenticator.get(),
      callDataService = callDataServiceFactory.create(client),
      wasmoDbService = wasmoDbService,
      inviteService = inviteService,
    )
  }

  fun authenticate(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ) = authenticatePasskeyAction().authenticate(
    request = AuthenticatePasskeyRequest(
      authentication = passkey.authentication(
        challenge = challenger.create(),
        origin = deployment.baseUrl.toString(),
      ),
      inviteCode = inviteCode,
    ),
  )

  fun installAppAction() = InstallAppAction(
    client = clientAuthenticator.get(),
    computerStore = computerStore,
    wasmoDbService = wasmoDbService,
  )

  fun createComputer(slug: ComputerSlug): ComputerTester {
    // Create a ComputerSpec.
    val createComputerResponse = createComputerAction()
      .create(
        request = CreateComputerRequest(
          computerSpecToken = newToken(),
          slug = slug,
        ),
      )

    // Pay for it.
    val checkoutSessionId = paymentsService.completePayment(
      createComputerResponse.body.checkoutSessionClientSecret,
    )

    // Sync payment state.
    afterCheckoutAction().get(checkoutSessionId)

    return ComputerTester(
      slug = slug,
    )
  }
}

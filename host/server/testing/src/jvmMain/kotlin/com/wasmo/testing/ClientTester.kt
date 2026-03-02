package com.wasmo.testing

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.RealAccountStore
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.invite.InvitePageAction
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.ComputerSlug
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.app.db.WasmoDbService
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

  val accountStoreFactory = RealAccountStore.Factory(
    authenticatorDatabase = authenticatorDatabase,
    wasmoDbService = wasmoDbService,
  )

  val passkeyChecker = RealPasskeyChecker(
    challenger = challenger,
    deployment = deployment,
  )

  val passkeyLinkerFactory = PasskeyLinker.Factory(
    cookieQueries = wasmoDbService.cookieQueries,
  )

  val inviteService = InviteService(
    clock = clock,
    wasmoDbService = wasmoDbService,
  )

  val hostPageFactory = RealServerHostPage.Factory(
    deployment = deployment,
    stripePublishableKey = stripePublishableKey,
  )

  fun routingContext() = RoutingContext(
    rootUrl = deployment.baseUrl.toString(),
    hasComputers = false,
    hasInvite = false,
    isAdmin = false,
  )

  fun routeCodec(): RouteCodec = RealRouteCodec(
    routingContext = routingContext(),
  )

  fun accountSnapshotAction() = AccountSnapshotAction(
    accountStoreFactory = accountStoreFactory,
    client = clientAuthenticator.get(),
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

  fun registerPasskeyAction() = RegisterPasskeyAction(
    clock = clock,
    accountStoreFactory = accountStoreFactory,
    client = clientAuthenticator.get(),
    passkeyChecker = passkeyChecker,
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

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
    client = clientAuthenticator.get(),
    accountStoreFactory = accountStoreFactory,
    hostPageFactory = hostPageFactory,
    wasmoDbService = wasmoDbService,
  )

  fun authenticatePasskeyAction() = AuthenticatePasskeyAction(
    accountStoreFactory = accountStoreFactory,
    client = clientAuthenticator.get(),
    passkeyChecker = passkeyChecker,
    passkeyLinkerFactory = passkeyLinkerFactory,
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

  fun createComputerAction() = CreateComputerAction(
    paymentsService = paymentsService,
    client = clientAuthenticator.get(),
    wasmoDbService = wasmoDbService,
    computerSpecStore = computerSpecStore,
  )

  fun afterCheckoutAction() = AfterCheckoutAction(
    paymentsService = paymentsService,
    subscriptionUpdater = subscriptionUpdater,
    routeCodec = routeCodec(),
    client = clientAuthenticator.get(),
  )

  fun createInviteAction() = CreateInviteAction(
    client = clientAuthenticator.get(),
    routeCodec = routeCodec(),
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

  fun invitePageAction() = InvitePageAction(
    client = clientAuthenticator.get(),
    accountStoreFactory = accountStoreFactory,
    hostPageFactory = hostPageFactory,
    wasmoDbService = wasmoDbService,
  )

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

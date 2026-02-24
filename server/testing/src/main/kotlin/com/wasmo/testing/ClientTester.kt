package com.wasmo.testing

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.RealAccountStore
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.sendemail.SendEmailService
import kotlin.time.Clock

class ClientTester(
  private val clock: Clock,
  private val wasmoDbService: WasmoDbService,
  private val deployment: Deployment,
  private val sendEmailService: SendEmailService,
  private val clientAuthenticator: ClientAuthenticator,
  private val computerStore: ComputerStore,
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

  fun routingContext() = RoutingContext(
    rootUrl = deployment.baseUrl.toString(),
    hasComputers = false,
    hasInvite = false,
    isAdmin = false,
  )

  fun routeCodec(): RouteCodec = RealRouteCodec(
    context = routingContext(),
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

  fun authenticatePasskeyAction() = AuthenticatePasskeyAction(
    accountStoreFactory = accountStoreFactory,
    client = clientAuthenticator.get(),
    passkeyChecker = passkeyChecker,
    passkeyLinkerFactory = passkeyLinkerFactory,
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
  )

  fun createInviteAction() = CreateInviteAction(
    client = clientAuthenticator.get(),
    routeCodec = routeCodec(),
    wasmoDbService = wasmoDbService,
    inviteService = inviteService,
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

  fun createComputerAction() = CreateComputerAction(
    client = clientAuthenticator.get(),
    computerStore = computerStore,
  )

  fun installAppAction() = InstallAppAction(
    client = clientAuthenticator.get(),
    computerStore = computerStore,
  )

  fun createComputer(slug: String): ComputerTester {
    createComputerAction().createComputer(
      request = CreateComputerRequest(
        slug = slug,
      ),
    )

    return ComputerTester(
      slug = slug,
    )
  }
}

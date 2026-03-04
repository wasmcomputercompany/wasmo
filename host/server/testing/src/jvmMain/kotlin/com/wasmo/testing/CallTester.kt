package com.wasmo.testing

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.Url
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.deployment.Deployment
import com.wasmo.website.HostPageAction
import com.wasmo.website.ServerHostPage
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@Inject
class CallTester(
  val deployment: Deployment,
  val challenger: Challenger,
  val accountSnapshotActionProvider: Provider<AccountSnapshotAction>,
  val linkEmailAddressActionProvider: Provider<LinkEmailAddressAction>,
  val confirmEmailAddressActionProvider: Provider<ConfirmEmailAddressAction>,
  val registerPasskeyActionProvider: Provider<RegisterPasskeyAction>,
  val hostPageActionProvider: Provider<HostPageAction>,
  val authenticatePasskeyActionProvider: Provider<AuthenticatePasskeyAction>,
  val createComputerActionProvider: Provider<CreateComputerAction>,
  val afterCheckoutActionProvider: Provider<AfterCheckoutAction>,
  val createInviteActionProvider: Provider<CreateInviteAction>,
  val installAppActionProvider: Provider<InstallAppAction>,
  val routeCodecFactory: RouteCodec.Factory,
) {
  fun routingContext() = RoutingContext(
    rootUrl = deployment.baseUrl.toString(),
    hasComputers = false,
    hasInvite = false,
    isAdmin = false,
  )

  fun routeCodec() = routeCodecFactory.create(
    routingContext = routingContext(),
  )

  fun accountSnapshot(request: AccountSnapshotRequest) =
    accountSnapshotActionProvider().get(request)

  suspend fun linkEmailAddress(request: LinkEmailAddressRequest) =
    linkEmailAddressActionProvider().link(request)

  fun confirmEmailAddress(request: ConfirmEmailAddressRequest) =
    confirmEmailAddressActionProvider().confirm(request)

  fun registerPasskey(request: RegisterPasskeyRequest) =
    registerPasskeyActionProvider().register(request)

  fun hostPage(url: Url): ServerHostPage =
    hostPageActionProvider().get(url)

  fun hostPage(route: Route): ServerHostPage =
    hostPage(
      url = routeCodec().encode(route),
    )

  fun authenticatePasskey(request: AuthenticatePasskeyRequest) =
    authenticatePasskeyActionProvider().authenticate(request)

  fun createComputer(request: CreateComputerRequest) =
    createComputerActionProvider().create(request)

  fun afterCheckout(checkoutSessionId: String) =
    afterCheckoutActionProvider().get(checkoutSessionId)

  fun createInvite(request: CreateInviteRequest) =
    createInviteActionProvider().create(request)

  suspend fun installApp(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ) = installAppActionProvider().install(computerSlug, request)

  fun authenticate(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ) = authenticatePasskey(
    request = AuthenticatePasskeyRequest(
      authentication = passkey.authentication(
        challenge = challenger.create(),
        origin = deployment.baseUrl.toString(),
      ),
      inviteCode = inviteCode,
    ),
  )
}

package com.wasmo.testing

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.ConfirmEmailAddressAction
import com.wasmo.accounts.LinkEmailAddressAction
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import com.wasmo.website.HostPageAction
import com.wasmo.website.ServerHostPage
import dev.zacsweers.metro.Inject

@Inject
class CallTester(
  val deployment: Deployment,
  val challenger: Challenger,
  val accountSnapshotAction: AccountSnapshotAction,
  val linkEmailAddressAction: LinkEmailAddressAction,
  val confirmEmailAddressAction: ConfirmEmailAddressAction,
  val registerPasskeyAction: RegisterPasskeyAction,
  val hostPageAction: HostPageAction,
  val authenticatePasskeyAction: AuthenticatePasskeyAction,
  val createComputerAction: CreateComputerAction,
  val afterCheckoutAction: AfterCheckoutAction,
  val createInviteAction: CreateInviteAction,
  val installAppAction: InstallAppAction,
  val routeCodecFactory: RouteCodec.Factory,
) {
  fun routingContext() = RoutingContext(
    rootUrl = deployment.baseUrl.toString(),
    hasComputers = false,
    hasInvite = false,
    isAdmin = false,
  )

  fun routeCodec(): RouteCodec = routeCodecFactory.create(
    routingContext = routingContext(),
  )

  fun authenticate(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ): Response<AuthenticatePasskeyResponse> = authenticatePasskeyAction.authenticate(
    request = AuthenticatePasskeyRequest(
      authentication = passkey.authentication(
        challenge = challenger.create(),
        origin = deployment.baseUrl.toString(),
      ),
      inviteCode = inviteCode,
    ),
  )

  fun hostPage(route: Route): ServerHostPage {
    val url = routeCodec().encode(route)
    return hostPageAction.get(url)
  }
}

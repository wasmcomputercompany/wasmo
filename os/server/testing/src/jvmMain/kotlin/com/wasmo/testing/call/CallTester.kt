package com.wasmo.testing.call

import com.wasmo.accounts.AccountSnapshotRpc
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.SignOutPage
import com.wasmo.accounts.SignOutRpc
import com.wasmo.accounts.invite.CreateInviteRpc
import com.wasmo.accounts.passkeys.AuthenticatePasskeyRpc
import com.wasmo.accounts.passkeys.RegisterPasskeyRpc
import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateUsernameRequest
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkUsernameRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.SignOutRequest
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.computers.InstallAppRpc
import com.wasmo.computers.paid.PaidCreateComputerSpecRpc
import com.wasmo.emails.ConfirmEmailAddressRpc
import com.wasmo.emails.LinkEmailAddressRpc
import com.wasmo.framework.Request
import com.wasmo.framework.Url
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.Deployment
import com.wasmo.installedapps.CallAppAction
import com.wasmo.stripe.AfterCheckoutPage
import com.wasmo.support.tokens.ChallengeCode
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.framework.snapshot
import com.wasmo.usernames.CreateUsernameRpc
import com.wasmo.usernames.LinkUsernameRpc
import com.wasmo.website.OsPage
import com.wasmo.website.ServerOsHtml
import dev.zacsweers.metro.Inject
import okhttp3.HttpUrl
import okio.ByteString
import wit.wasmo.http.Header

/**
 * Tests a single API call to the server.
 */
@Inject
class CallTester(
  private val deployment: Deployment,
  private val challenger: Challenger,
  private val accountSnapshotRpcProvider: () -> AccountSnapshotRpc,
  private val afterCheckoutPageProvider: () -> AfterCheckoutPage,
  private val authenticatePasskeyRpcProvider: () -> AuthenticatePasskeyRpc,
  private val callAppActionProvider: () -> CallAppAction,
  private val confirmEmailAddressRpcProvider: () -> ConfirmEmailAddressRpc,
  private val createComputerSpecRpcProvider: () -> PaidCreateComputerSpecRpc,
  private val createInviteRpcProvider: () -> CreateInviteRpc,
  private val createUsernameRpcProvider: () -> CreateUsernameRpc,
  private val installAppRpcProvider: () -> InstallAppRpc,
  private val linkEmailAddressRpcProvider: () -> LinkEmailAddressRpc,
  private val linkUsernameRpcProvider: () -> LinkUsernameRpc,
  private val osPageProvider: () -> OsPage,
  private val registerPasskeyRpcProvider: () -> RegisterPasskeyRpc,
  private val signOutRpcProvider: () -> SignOutRpc,
  private val signOutPageProvider: () -> SignOutPage,
  private val routeCodecFactory: RouteCodec.Factory,
) {
  fun routingContext() = RoutingContext(
    rootUrl = deployment.baseUrl.toString(),
  )

  fun routeCodec() = routeCodecFactory.create(
    routingContext = routingContext(),
  )

  fun createChallenge() = challenger.create()

  suspend fun accountSnapshot(request: AccountSnapshotRequest) =
    accountSnapshotRpcProvider().get(request)

  suspend fun afterCheckout(checkoutSessionId: String) =
    afterCheckoutPageProvider().get(checkoutSessionId)

  suspend fun authenticate(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ) = authenticatePasskey(
    request = AuthenticatePasskeyRequest(
      authentication = passkey.authentication(
        challenge = createChallenge(),
        origin = deployment.baseUrl.toString(),
      ),
      inviteCode = inviteCode,
    ),
  )

  suspend fun authenticatePasskey(request: AuthenticatePasskeyRequest) =
    authenticatePasskeyRpcProvider().authenticate(request)

  suspend fun callApp(request: Request) =
    callAppActionProvider().call(request).snapshot()

  suspend fun callApp(
    method: String = "GET",
    url: HttpUrl,
    headers: List<Header> = listOf(),
    body: ByteString? = null,
  ) = callApp(
    Request(
      method = method,
      url = url,
      headers = headers,
      body = body,
    ),
  )

  suspend fun createUsername(request: CreateUsernameRequest) =
    createUsernameRpcProvider().handle(request.username, AccountPassword(request.password))

  suspend fun linkUsername(request: LinkUsernameRequest) =
    linkUsernameRpcProvider().handle(request.username, AccountPassword(request.password))

  suspend fun confirmEmailAddress(request: ConfirmEmailAddressRequest) =
    confirmEmailAddressRpcProvider().confirm(request)

  suspend fun confirmEmailAddress(
    emailAddress: String,
    challengeToken: String,
    challengeCode: ChallengeCode,
  ) = confirmEmailAddress(
    request = ConfirmEmailAddressRequest(
      unverifiedEmailAddress = emailAddress,
      challengeToken = challengeToken,
      challengeCode = challengeCode.value,
    ),
  )

  suspend fun createComputerSpec(request: CreateComputerSpecRequest) =
    createComputerSpecRpcProvider().create(request)

  suspend fun createInvite(request: CreateInviteRequest) =
    createInviteRpcProvider().create(request)

  suspend fun installApp(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ) = installAppRpcProvider().install(computerSlug, request)

  suspend fun linkEmailAddress(request: LinkEmailAddressRequest) =
    linkEmailAddressRpcProvider().link(request)

  suspend fun linkEmailAddress(emailAddress: String) =
    linkEmailAddress(
      LinkEmailAddressRequest(
        unverifiedEmailAddress = emailAddress,
      ),
    )

  suspend fun osPage(url: Url): ServerOsHtml =
    osPageProvider().get(url)

  suspend fun osPage(route: Route): ServerOsHtml = osPage(
    url = routeCodec().encode(route),
  )

  suspend fun registerPasskey(request: RegisterPasskeyRequest) =
    registerPasskeyRpcProvider().register(request)

  suspend fun signOut(request: SignOutRequest) =
    signOutRpcProvider().signOut(request)

  suspend fun signOutPage() = signOutPageProvider().get()
}

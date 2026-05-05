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
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.SignOutRequest
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.computers.CreateComputerSpecRpc
import com.wasmo.computers.InstallAppRpc
import com.wasmo.emails.ConfirmEmailAddressRpc
import com.wasmo.emails.LinkEmailAddressRpc
import com.wasmo.framework.Request
import com.wasmo.framework.Url
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.Deployment
import com.wasmo.installedapps.CallAppAction
import com.wasmo.stripe.AfterCheckoutPage
import com.wasmo.support.tokens.ChallengeCode
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.framework.snapshot
import com.wasmo.website.OsPage
import com.wasmo.website.ServerOsHtml
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import okhttp3.HttpUrl
import okio.ByteString
import wasmo.http.Header

/**
 * Tests a single API call to the server.
 */
@Inject
class CallTester(
  private val deployment: Deployment,
  private val challenger: Challenger,
  private val accountSnapshotRpcProvider: Provider<AccountSnapshotRpc>,
  private val afterCheckoutPageProvider: Provider<AfterCheckoutPage>,
  private val authenticatePasskeyRpcProvider: Provider<AuthenticatePasskeyRpc>,
  private val callAppActionProvider: Provider<CallAppAction>,
  private val confirmEmailAddressRpcProvider: Provider<ConfirmEmailAddressRpc>,
  private val createComputerSpecRpcProvider: Provider<CreateComputerSpecRpc>,
  private val createInviteRpcProvider: Provider<CreateInviteRpc>,
  private val installAppRpcProvider: Provider<InstallAppRpc>,
  private val linkEmailAddressRpcProvider: Provider<LinkEmailAddressRpc>,
  private val osPageProvider: Provider<OsPage>,
  private val registerPasskeyRpcProvider: Provider<RegisterPasskeyRpc>,
  private val signOutRpcProvider: Provider<SignOutRpc>,
  private val signOutPageProvider: Provider<SignOutPage>,
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

package com.wasmo.testing.call

import com.wasmo.accounts.AccountSnapshotAction
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.invite.CreateInviteAction
import com.wasmo.accounts.passkeys.AuthenticatePasskeyAction
import com.wasmo.accounts.passkeys.RegisterPasskeyAction
import com.wasmo.api.AccountSnapshotRequest
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.Url
import com.wasmo.computers.AfterCheckoutAction
import com.wasmo.computers.CreateComputerSpecAction
import com.wasmo.deployment.Deployment
import com.wasmo.emails.ConfirmEmailAddressAction
import com.wasmo.emails.LinkEmailAddressAction
import com.wasmo.framework.Request
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.installedapps.CallAppAction
import com.wasmo.installedapps.InstallAppAction
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.framework.snapshot
import com.wasmo.website.OsPageAction
import com.wasmo.website.ServerOsPage
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
  private val accountSnapshotActionProvider: Provider<AccountSnapshotAction>,
  private val linkEmailAddressActionProvider: Provider<LinkEmailAddressAction>,
  private val confirmEmailAddressActionProvider: Provider<ConfirmEmailAddressAction>,
  private val registerPasskeyActionProvider: Provider<RegisterPasskeyAction>,
  private val osPageActionProvider: Provider<OsPageAction>,
  private val authenticatePasskeyActionProvider: Provider<AuthenticatePasskeyAction>,
  private val createComputerSpecActionProvider: Provider<CreateComputerSpecAction>,
  private val afterCheckoutActionProvider: Provider<AfterCheckoutAction>,
  private val createInviteActionProvider: Provider<CreateInviteAction>,
  private val installAppActionProvider: Provider<InstallAppAction>,
  private val callAppActionProvider: Provider<CallAppAction>,
  private val routeCodecFactory: RouteCodec.Factory,
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

  fun createChallenge() = challenger.create()

  suspend fun accountSnapshot(request: AccountSnapshotRequest) =
    accountSnapshotActionProvider().get(request)

  suspend fun linkEmailAddress(request: LinkEmailAddressRequest) =
    linkEmailAddressActionProvider().link(request)

  fun confirmEmailAddress(request: ConfirmEmailAddressRequest) =
    confirmEmailAddressActionProvider().confirm(request)

  suspend fun registerPasskey(request: RegisterPasskeyRequest) =
    registerPasskeyActionProvider().register(request)

  suspend fun osPage(url: Url): ServerOsPage =
    osPageActionProvider().get(url)

  suspend fun osPage(route: Route): ServerOsPage = osPage(
    url = routeCodec().encode(route),
  )

  suspend fun authenticatePasskey(request: AuthenticatePasskeyRequest) =
    authenticatePasskeyActionProvider().authenticate(request)

  suspend fun createComputerSpec(request: CreateComputerSpecRequest) =
    createComputerSpecActionProvider().create(request)

  suspend fun afterCheckout(checkoutSessionId: String) =
    afterCheckoutActionProvider().get(checkoutSessionId)

  suspend fun createInvite(request: CreateInviteRequest) =
    createInviteActionProvider().create(request)

  suspend fun installApp(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ) = installAppActionProvider().install(computerSlug, request)

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
}

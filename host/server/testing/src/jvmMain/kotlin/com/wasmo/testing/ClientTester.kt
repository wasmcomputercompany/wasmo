package com.wasmo.testing

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.api.ComputerSlug
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.common.tokens.newToken
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString

@Inject
@SingleIn(ClientScope::class)
class ClientTester(
  private val deployment: Deployment,
  private val clientAuthenticator: ClientAuthenticator,
  val paymentsService: FakePaymentsService,
  val callTesterGraphFactory: CallTesterGraph.Factory,
) {
  fun call(): CallTester {
    val client = clientAuthenticator.get()
    val callTesterGraph = callTesterGraphFactory.create(client)
    return callTesterGraph.callTester
  }

  fun createChallenge(): ByteString = call().challenger.create()

  fun register(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ): Response<RegisterPasskeyResponse> = call().registerPasskeyAction.register(
    request = RegisterPasskeyRequest(
      registration = passkey.registration(
        challenge = createChallenge(),
        origin = deployment.baseUrl.toString(),
      ),
      inviteCode = inviteCode,
    ),
  )

  fun createComputer(slug: ComputerSlug): ComputerTester {
    // Create a ComputerSpec.
    val createComputerResponse = call().createComputerAction.create(
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
    call().afterCheckoutAction.get(checkoutSessionId)

    return ComputerTester(
      slug = slug,
    )
  }
}

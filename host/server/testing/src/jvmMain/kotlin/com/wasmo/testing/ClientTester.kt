package com.wasmo.testing

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.api.ComputerSlug
import com.wasmo.api.CreateComputerSpecRequest
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
  private val callTesterGraphFactory: CallTesterGraph.Factory,
  val paymentsService: FakePaymentsService,
) {
  fun call(): CallTester {
    val client = clientAuthenticator.get()
    val callTesterGraph = callTesterGraphFactory.create(client)
    return callTesterGraph.callTester
  }

  fun createChallenge(): ByteString = call().createChallenge()

  fun register(
    passkey: FakePasskey,
    inviteCode: String? = null,
  ): Response<RegisterPasskeyResponse> = call().registerPasskey(
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
    val createComputerSpecResponse = call().createComputerSpec(
      request = CreateComputerSpecRequest(
        computerSpecToken = newToken(),
        slug = slug,
      ),
    )

    // Pay for it.
    val checkoutSessionId = paymentsService.completePayment(
      createComputerSpecResponse.body.checkoutSessionClientSecret,
    )

    // Sync payment state.
    call().afterCheckout(checkoutSessionId)

    return ComputerTester(
      slug = slug,
    )
  }
}

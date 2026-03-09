package com.wasmo.testing.client

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.common.tokens.newToken
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.FakePaymentsService
import com.wasmo.testing.call.CallTester
import com.wasmo.testing.call.CallTesterGraph
import com.wasmo.testing.computer.ComputerTester
import com.wasmo.testing.computer.ComputerTesterGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString

/**
 * Tests on behalf of a single user.
 */
@Inject
@SingleIn(ClientScope::class)
class ClientTester(
  private val deployment: Deployment,
  private val clientAuthenticator: ClientAuthenticator,
  private val callTesterGraphFactory: CallTesterGraph.Factory,
  private val computerTesterGraphFactory: ComputerTesterGraph.Factory,
  val paymentsService: FakePaymentsService,
) {
  private var nextComputerSlug: Int = 100

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

  fun createComputer(
    slug: ComputerSlug = ComputerSlug("computer${nextComputerSlug++}"),
  ): ComputerTester {
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

    val graph = computerTesterGraphFactory.create(slug)
    return graph.computerTester
  }
}

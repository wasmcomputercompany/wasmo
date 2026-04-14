package com.wasmo.testing.client

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.SessionCookie
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.support.tokens.newToken
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.FakePaymentsService
import com.wasmo.testing.call.CallTester
import com.wasmo.testing.call.CallTesterGraph
import com.wasmo.testing.computer.ComputerTester
import com.wasmo.testing.events.TestEventListener
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import okio.ByteString

/**
 * Tests on behalf of a single user.
 */
@AssistedInject
class ClientTester(
  private val deployment: Deployment,
  private val callTesterGraphFactory: CallTesterGraph.Factory,
  private val eventListener: TestEventListener,
  private val computerTesterFactory: ComputerTester.Factory,
  val paymentsService: FakePaymentsService,
  @Assisted val clientAuthenticator: ClientAuthenticator,
  @Assisted private val sessionCookie: SessionCookie,
) {
  private var nextComputerSlug: Int = 100

  fun call(): CallTester {
    val client = clientAuthenticator.get()
    val graph = callTesterGraphFactory.create(client, sessionCookie)
    return graph.callTester
  }

  fun createChallenge(): ByteString = call().createChallenge()

  suspend fun register(
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

  suspend fun createComputer(
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

    // Wait for installation and discard installation-related events.
    eventListener.awaitIdle()
    eventListener.receiveAll()

    return getComputer(slug)
  }

  fun getComputer(slug: ComputerSlug) = computerTesterFactory.create(
    client = this,
    slug = slug,
  )

  @AssistedFactory
  interface Factory {
    fun create(
      clientAuthenticator: ClientAuthenticator,
      sessionCookie: SessionCookie,
    ): ClientTester
  }
}

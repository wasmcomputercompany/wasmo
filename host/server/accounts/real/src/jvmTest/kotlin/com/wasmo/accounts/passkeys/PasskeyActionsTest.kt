package com.wasmo.accounts.passkeys

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.PasskeySnapshot
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.routes.TeaserRoute
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PasskeyActionsTest {
  lateinit var tester: WasmoServiceTester

  @BeforeTest
  fun setUp() {
    tester = WasmoServiceTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() {
    val onlyPasskey = tester.newPasskey()

    val clientA = tester.newClient()
    val registration = onlyPasskey.registration(
      clientA.createChallenge(),
      tester.origin,
    )

    val registerResponse = clientA.call().registerPasskey(RegisterPasskeyRequest(registration))
    val passkeySnapshot = PasskeySnapshot(
      authenticator = "Apple Passwords",
      createdAt = tester.clock.now(),
    )
    assertThat(registerResponse.body.account.passkeys)
      .containsExactly(passkeySnapshot)
    assertThat(clientA.call().hostPage(TeaserRoute).accountSnapshot.passkeys)
      .containsExactly(passkeySnapshot)

    val clientB = tester.newClient()
    val authenticateResponse = clientB.call().authenticatePasskey(
      AuthenticatePasskeyRequest(
        onlyPasskey.authentication(
          clientB.createChallenge(),
          tester.origin,
        ),
      ),
    )
    assertThat(authenticateResponse.body.account.passkeys).containsExactly(
      PasskeySnapshot(
        authenticator = "Apple Passwords",
        createdAt = tester.clock.now(),
      ),
    )
  }

  @Test
  fun registerIsIdempotent() {
    val onlyPasskey = tester.newPasskey()

    val clientA = tester.newClient()
    val registerResponse1 = clientA.register(onlyPasskey)
    val registerResponse2 = clientA.register(onlyPasskey)
    assertThat(registerResponse2.body).isEqualTo(registerResponse1.body)
  }

  @Test
  fun authenticateIsIdempotent() {
    val onlyPasskey = tester.newPasskey()

    val clientA = tester.newClient()
    clientA.register(onlyPasskey)

    val clientB = tester.newClient()
    val authenticateResponse1 = clientB.call().authenticate(onlyPasskey)
    val authenticateResponse2 = clientB.call().authenticate(onlyPasskey)
    assertThat(authenticateResponse2.body).isEqualTo(authenticateResponse1.body)
  }

  @Test
  fun authenticatePasskeyGrantsAccessToAssets() {
    val onlyPasskey = tester.newPasskey()

    val clientA = tester.newClient()
    clientA.register(onlyPasskey)

    val clientB = tester.newClient()
    clientB.call().authenticate(onlyPasskey)
  }
}

package com.wasmo.passkeys

import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.PasskeySnapshot
import com.wasmo.api.RegisterPasskeyRequest
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
      clientA.challenger.create(),
      tester.origin,
    )

    val registerResponse = clientA.registerPasskeyAction()
      .register(RegisterPasskeyRequest(registration))
    assertThat(registerResponse.body.account.passkeys).containsExactly(
      PasskeySnapshot(
        authenticator = "Apple Passwords",
        createdAt = tester.clock.now(),
      ),
    )

    val clientB = tester.newClient()
    val authenticateResponse = clientB.authenticatePasskeyAction().authenticate(
      AuthenticatePasskeyRequest(
        onlyPasskey.authentication(
          clientB.challenger.create(),
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
  fun authenticatePasskeyGrantsAccessToAssets() {
    val onlyPasskey = tester.newPasskey()

    val clientA = tester.newClient()
    clientA.register(onlyPasskey)

    val clientB = tester.newClient()
    clientB.authenticate(onlyPasskey)
  }
}

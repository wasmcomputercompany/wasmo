package com.wasmo.admin.server

import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.hello.api.GreetRequest
import com.wasmo.hello.server.HelloAppTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class GreetActionTest {
  private lateinit var tester: HelloAppTester

  @BeforeTest
  fun setUp() {
    tester = HelloAppTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() = runTest {
    val action = tester.app.greetAction()

    val response1 = action.greet(GreetRequest(name = "Jesse"))
    assertThat(response1.recentNames).containsExactly("Jesse")

    // Ensure we get distinct timestamps.
    tester.clock.now += 1.seconds

    val response2 = action.greet(GreetRequest(name = "Mike"))
    assertThat(response2.recentNames).containsExactly("Mike", "Jesse")
  }
}

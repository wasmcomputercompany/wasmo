package com.wasmo.journal.server

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.journal.api.GreetRequest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class GreetActionTest {
  @InterceptTest
  val tester = JournalAppTester()

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

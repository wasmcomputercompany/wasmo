package com.wasmo.emails.messages

import app.cash.burst.InterceptTest
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import com.wasmo.support.tokens.toChallengeCodeOrNull
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest

class ChallengeCodeEmailTest {
  @InterceptTest
  val snapshotTester = SnapshotTester()

  @Test
  fun happyPath() = runTest {
    val email = challengeCodeEmailMessage(
      from = "noreply@wasmo.com",
      to = "jesse@swank.ca",
      baseUrl = "https://wasmo.com/",
      baseUrlHost = "wasmo.com",
      challengeCode = "654321".toChallengeCodeOrNull()!!,
    )

    document.body!!.innerHTML = email.html
    snapshotTester.snapshot(document.body!!, Frame.Companion.Iphone14)
  }
}

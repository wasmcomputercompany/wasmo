package com.wasmo.accounts.emails

import app.cash.burst.InterceptTest
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import okio.Buffer

class ChallengeCodeEmailTest {
  @InterceptTest
  val snapshotTester = SnapshotTester()

  @Test
  fun happyPath() = runTest {
    val email = ChallengeCodeEmail(
      baseUrl = "https://wasmo.com/",
      baseUrlHost = "wasmo.com",
      code = "654321",
      recipientEmailAddress = "jesse@swank.ca",
    )

    document.body!!.innerHTML = Buffer().run {
      email.write(this)
      readUtf8()
    }

    snapshotTester.snapshot(document.body!!, Frame.Iphone14)
  }
}

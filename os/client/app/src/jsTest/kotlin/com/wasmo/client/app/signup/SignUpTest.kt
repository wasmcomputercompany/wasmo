package com.wasmo.client.app.signup

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SignUpTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/pico-for-wasmo.css",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun enterEmailAddress() = runTest {
    snapshotTester.snapshot {
      EnterEmailAddressScreen(
        emailAddress = "",
        emailAddressCaption = "We’ll email you a challenge code",
        canSubmit = true,
        busy = false,
        disabled = false,
        eventListener = {
        },
      )
    }
  }

  @Test
  fun enterChallengeCode() = runTest {
    snapshotTester.snapshot {
      EnterChallengeCodeScreen(
        challengeCode = "",
        challengeCodeCaption = "Enter the code sent to jesse@example.com",
        canSubmit = true,
        eventListener = {
        },
      )
    }
  }
}

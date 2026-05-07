package com.wasmo.client.app.signup

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import app.cash.burst.burstValues
import com.wasmo.domtester.DarkMode
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

@Burst
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
  fun enterEmailAddress(
    darkMode: DarkMode,
    frame: Frame = burstValues(
      Frame.Iphone14,
      Frame.Iphone14Landscape,
      Frame.Ipad11,
      Frame.MacBookAir13,
    ),
  ) = runTest {
    snapshotTester.snapshot(
      frame = frame,
      darkMode = darkMode,
      background = "var(--pico-background-color)",
    ) {
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
        busy = false,
        disabled = false,
        eventListener = {
        },
      )
    }
  }
}

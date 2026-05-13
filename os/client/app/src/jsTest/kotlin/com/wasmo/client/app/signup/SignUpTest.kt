package com.wasmo.client.app.signup

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import app.cash.burst.burstValues
import com.wasmo.domtester.DarkMode
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import com.wasmo.identifiers.UsernameSlug
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

@Burst
class SignUpTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Figtree:ital,wght@0,300..900;1,300..900&display=swap",
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
        attrs = {
          classes("FillWidthHeight")
        },
        emailAddress = "",
        emailAddressHelperText = "We’ll email you a challenge code",
        canSubmit = true,
        busy = false,
        disabled = false,
        eventListener = {
        },
      )
    }
  }

  @Test
  fun selectUsernameToSignIn(
    darkMode: DarkMode,
  ) = runTest {
    snapshotTester.snapshot(
      darkMode = darkMode,
      background = "var(--pico-background-color)",
    ) {
      SelectUsernameToSignInScreen(
        attrs = {
          classes("FillWidthHeight")
        },
        options = listOf(
          UsernameSlug("admin"),
          UsernameSlug("jesse"),
        ),
        canSubmit = true,
        busy = false,
        canCreateUsername = true,
        eventListener = {
        },
      )
    }
  }

  @Test
  fun newAccountWithUsername(
    darkMode: DarkMode,
  ) = runTest {
    snapshotTester.snapshot(
      darkMode = darkMode,
      background = "var(--pico-background-color)",
    ) {
      NewAccountWithUsernameScreen(
        attrs = {
          classes("FillWidthHeight")
        },
        username = "",
        usernameHelperText = "",
        disabled = false,
        canSubmit = true,
        busy = false,
        eventListener = {
        },
      )
    }
  }

  @Test
  fun enterChallengeCode() = runTest {
    snapshotTester.snapshot {
      EnterChallengeCodeScreen(
        attrs = {
          classes("FillWidthHeight")
        },
        challengeCode = "",
        challengeCodeHelperText = "Enter the code sent to jesse@example.com",
        canSubmit = true,
        busy = false,
        disabled = false,
        eventListener = {
        },
      )
    }
  }
}

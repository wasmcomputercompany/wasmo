package com.wasmo.client.app.pico

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class PicoTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/pico-for-wasmo.css",
      "/assets/wasmo2.css",
    ),
  )

  @Test
  fun enterEmailAddress() = runTest {
    snapshotTester.snapshot {
      ScreenFrame {
        PicoSampleScreen(
          emailAddress = "",
          emailAddressCaption = "We’ll email you a challenge code",
          canSubmit = true,
          eventListener = {
          },
        )
      }
    }
  }
}

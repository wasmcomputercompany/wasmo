package com.wasmo.client.app.pico

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import app.cash.burst.burstValues
import com.wasmo.domtester.DarkMode
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

@Burst
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
      PicoSampleScreen(
        emailAddress = "",
        emailAddressCaption = "We’ll email you a challenge code",
        canSubmit = true,
        eventListener = {
        },
      )
    }
  }

  @Test
  fun launcher(
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
      PicoLauncherIconList {
        PicoLauncherIcon("Files", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Library", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Music", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Photos", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Pink Journal", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Recipes", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Smart Home", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Snake", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Writer", "/assets/launcher/sample-icon.svg")
        PicoLauncherIcon("Zap", "/assets/launcher/sample-icon.svg")
      }
    }
  }
}

package com.wasmo.compose

import app.cash.burst.Burst
import app.cash.burst.InterceptTest
import app.cash.burst.burstValues
import com.wasmo.domtester.DarkMode
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

@Burst
class LauncherTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/pico-for-wasmo.css",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun happyPath(
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
      LauncherScreen {
        LauncherIconList {
          Icon("Files", "/assets/launcher/sample-icon.svg")
          Icon("Library", "/assets/launcher/sample-icon.svg")
          Icon("Music", "/assets/launcher/sample-icon.svg")
          Icon("Photos", "/assets/launcher/sample-icon.svg")
          Icon("Pink Journal", "/assets/launcher/sample-icon.svg")
          Icon("Recipes", "/assets/launcher/sample-icon.svg")
          Icon("Smart Home", "/assets/launcher/sample-icon.svg")
          Icon("Snake", "/assets/launcher/sample-icon.svg")
          Icon("Writer", "/assets/launcher/sample-icon.svg")
          Icon("Zap", "/assets/launcher/sample-icon.svg")
        }
      }
    }
  }
}

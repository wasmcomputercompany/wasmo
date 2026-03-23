package com.wasmo.compose

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class LauncherTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun happyPath() = runTest {
    snapshotTester.snapshot {
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

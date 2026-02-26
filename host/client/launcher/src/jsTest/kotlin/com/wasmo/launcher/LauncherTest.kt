package com.wasmo.launcher

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
      "/assets/launcher/Launcher.css",
    ),
  )

  @Test
  fun happyPath() = runTest {
    snapshotTester.snapshot {
      LauncherScreen {
        LauncherIconList {
          Icon("Files", "/assets/launcher/sample-folder.svg")
          Icon("Library", "/assets/launcher/sample-books.svg")
          Icon("Music", "/assets/launcher/sample-headphones.svg")
          Icon("Photos", "/assets/launcher/sample-camera.svg")
          Icon("Pink Journal", "/assets/launcher/sample-flower.svg")
          Icon("Recipes", "/assets/launcher/sample-pancakes.svg")
          Icon("Smart Home", "/assets/launcher/sample-home.svg")
          Icon("Snake", "/assets/launcher/sample-snake.svg")
          Icon("Writer", "/assets/launcher/sample-w.svg")
          Icon("Zap", "/assets/launcher/sample-z.svg")
        }
      }
    }
  }
}

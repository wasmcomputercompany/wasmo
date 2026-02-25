package com.wasmo.client.app.buildyours

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class BuildYoursTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun initial() = runTest {
    snapshotTester.snapshot {
      BuildYoursScreen(
        showBuildForm = false,
      ) {
      }
    }
  }

  @Test
  fun buildForm() = runTest {
    snapshotTester.snapshot(
      scrolling = true,
    ) {
      BuildYoursScreen(
        showBuildForm = true,
      ) {
      }
    }
  }
}

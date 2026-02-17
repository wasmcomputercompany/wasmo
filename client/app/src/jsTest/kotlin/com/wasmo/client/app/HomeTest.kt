package com.wasmo.client.app

import app.cash.burst.InterceptTest
import com.wasmo.compose.ChildStyle
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class HomeTest {
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
      Home(ChildStyle {})
    }
  }
}

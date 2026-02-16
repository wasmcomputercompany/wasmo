package com.wasmo.client.app

import app.cash.burst.InterceptTest
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.web.renderComposableInBody

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
    renderComposableInBody {
      Home(ChildStyle {})
    }
    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
    )
  }
}

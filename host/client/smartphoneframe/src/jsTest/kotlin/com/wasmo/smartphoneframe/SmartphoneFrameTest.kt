package com.wasmo.smartphoneframe

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.web.css.Color.darkcyan
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

class SmartphoneFrameTest {
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
      SmartphoneFrame(
        attrs = {},
      ) { frameAttrs ->
        Div(
          attrs = {
            style {
              backgroundColor(darkcyan)
            }
            frameAttrs()
          },
        ) {
          H1 {
            Text("hello, I am a smart phone")
          }
        }
      }
    }
  }
}

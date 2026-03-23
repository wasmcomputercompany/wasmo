package com.wasmo.compose

import androidx.compose.runtime.Composable
import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class MenuTest {
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
      OverlayFrame { overlayFrameChildAttrs ->
        Menu(
          attrs = overlayFrameChildAttrs,
          visible = true,
          onDismiss = {},
          content = {
            MenuItem(
              label = "Install App",
              onClick = {},
            )
            MenuItem(
              label = "Settings",
              onClick = {},
            )
          },
        )
      }
    }
  }

  @Composable
  fun OverlayFrame(
    overlay: @Composable DOMScope<HTMLDivElement>.(
      attrs: AttrsScope<HTMLElement>.() -> Unit,
    ) -> Unit,
  ) {
    OverlayContainer(
      attrs = {
        style {
          width(100.percent)
          height(100.percent)
          boxSizing("border-box")
        }
      },
      showScrim = true,
      overlay = { overlayChildAttrs ->
        overlay(overlayChildAttrs)
      },
      content = { overlayChildAttrs ->
        Div(
          attrs = {
            style {
              backgroundColor(Color.cornflowerblue)
            }
            overlayChildAttrs()
          },
        )
      },
    )
  }
}

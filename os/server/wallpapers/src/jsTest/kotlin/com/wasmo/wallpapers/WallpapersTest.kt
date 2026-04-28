package com.wasmo.wallpapers

import app.cash.burst.InterceptTest
import com.wasmo.domtester.Frame
import com.wasmo.domtester.SnapshotTester
import com.wasmo.support.okiohtml.writeHtml
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import kotlinx.html.body
import kotlinx.html.div
import okio.Buffer

class WallpapersTest {
  @InterceptTest
  val snapshotTester = SnapshotTester()

  @Test
  fun happyPath() = runTest {
    for (wallpaper in BundledWallpapers) {
      snapshot(wallpaper)
    }
  }

  private suspend fun snapshot(wallpaper: Wallpaper) {
    document.body!!.innerHTML = Buffer().run {
      writeHtml {
        body {
          div {
            attributes["style"] =
              """
              |width: 100%;
              |height: 100%;
              |display: flex;
              |flex-direction: column;
              |flex-align: stretch;
              |""".trimMargin()
            div {
              attributes["style"] =
                """
                |flex: 100 100 0;
                |background: url('/assets/wallpapers/${wallpaper.filename}');
                |background-size: cover;
                |""".trimMargin()
            }
            div {
              attributes["style"] =
                """
                |flex: 100 100 0;
                |background: ${wallpaper.css};
                |""".trimMargin()
            }
          }
        }
      }
      readUtf8()
    }

    snapshotTester.snapshot(
      element = document.body!!,
      frame = Frame.Iphone14,
      name = wallpaper.filename,
    )
  }
}

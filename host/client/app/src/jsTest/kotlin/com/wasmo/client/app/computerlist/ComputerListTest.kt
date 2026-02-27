package com.wasmo.client.app.computerlist

import app.cash.burst.InterceptTest
import com.wasmo.api.routes.Url
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ComputerListTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun initial() = runTest {
    snapshotTester.snapshot(
      scrolling = true,
    ) {
      ComputerListScreen(
        items = listOf(
          ComputerListItem(
            slug = "jesse99",
            url = Url(
              scheme = "https",
              topPrivateDomain = "wasmo.com",
              subdomain = "jesse99",
            ),
          ),
          ComputerListItem(
            slug = "rounds",
            url = Url(
              scheme = "https",
              topPrivateDomain = "wasmo.com",
              subdomain = "rounds",
            ),
          ),
        ),
      ) {
      }
    }
  }
}

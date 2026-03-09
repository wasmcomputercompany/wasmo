package com.wasmo.client.app.computerlist

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import com.wasmo.identifiers.ComputerSlug
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
          Item(
            slug = ComputerSlug("jesse99"),
            iframeSrc = "https://jesse99.wasmo.com/",
          ),
          Item(
            slug = ComputerSlug("rounds"),
            iframeSrc = "https://rounds.wasmo.com/",
          ),
        ),
      ) {
      }
    }
  }
}

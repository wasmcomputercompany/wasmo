package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.burst.InterceptTest
import com.wasmo.client.app.computerlist.HomeScreenWithComputerList
import com.wasmo.client.app.computerlist.Item
import com.wasmo.domtester.SnapshotTester
import com.wasmo.identifiers.ComputerSlug
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

  private var menuModel by mutableStateOf<HomeMenuModel?>(null)
  private var teaser by mutableStateOf<Boolean>(true)

  @Test
  fun teaser() = runTest {
    teaser = true
    snapshotTester.snapshot {
      Subject()
    }

    menuModel = HomeMenuModel()
    snapshotTester.snapshot(name = "menuVisible") {
      Subject()
    }
  }

  @Test
  fun computerList() = runTest {
    teaser = false
    snapshotTester.snapshot(
      scrolling = true,
    ) {
      Subject()
    }

    menuModel = HomeMenuModel()
    snapshotTester.snapshot(
      name = "menuVisible",
      scrolling = true,
    ) {
      Subject()
    }
  }

  @Composable
  fun Subject() {
    if (teaser) {
      HomeScreenWithTeaser(
        showSignUp = true,
        scrimVisible = menuModel != null,
        menuModel = menuModel,
        eventListener = {},
      )
    } else {
      HomeScreenWithComputerList(
        scrimVisible = menuModel != null,
        menuModel = menuModel,
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
        eventListener = {},
      )
    }
  }
}

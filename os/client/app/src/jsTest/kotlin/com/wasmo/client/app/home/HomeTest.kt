package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.burst.InterceptTest
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

  private var menuModel by mutableStateOf<HomeMenuModel?>(null)

  @Test
  fun happyPath() = runTest {
    snapshotTester.snapshot {
      Subject()
    }

    menuModel = HomeMenuModel()
    snapshotTester.snapshot(name = "menuVisible") {
      Subject()
    }
  }

  @Composable
  fun Subject() {
    HomeScreen(
      showSignUp = true,
      scrimVisible = menuModel != null,
      menuModel = menuModel,
      eventListener = {},
    )
  }
}

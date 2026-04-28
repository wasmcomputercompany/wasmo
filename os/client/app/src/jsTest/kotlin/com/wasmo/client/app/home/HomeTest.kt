package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.burst.InterceptTest
import com.wasmo.client.app.computerlist.Item
import com.wasmo.domtester.SnapshotTester
import com.wasmo.identifiers.ComputerSlug
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest

class HomeTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  private var menuModelFlow = MutableStateFlow(
    HomeMenuModel(
      visible = false,
      signedIn = false,
    ),
  )
  private var showNewComputer by mutableStateOf(false)
  private var items by mutableStateOf<List<Item>>(listOf())

  @Test
  fun teaser() = runTest {
    snapshotTester.snapshot {
      Subject()
    }

    menuModelFlow.update { it.copy(visible = true) }
    snapshotTester.snapshot(name = "menuVisible") {
      Subject()
    }
  }

  @Test
  fun newComputerOnly() = runTest {
    showNewComputer = true
    snapshotTester.snapshot {
      Subject()
    }
  }

  @Test
  fun computerList() = runTest {
    showNewComputer = true
    items = listOf(
      Item(
        slug = ComputerSlug("jesse99"),
        iframeSrc = "https://jesse99.wasmo.com/",
      ),
      Item(
        slug = ComputerSlug("rounds"),
        iframeSrc = "https://rounds.wasmo.com/",
      ),
    )

    snapshotTester.snapshot(
      scrolling = true,
    ) {
      Subject()
    }
  }

  @Composable
  fun Subject() {
    val menuModel by menuModelFlow.collectAsState()
    HomeScreen(
      menuModel = menuModel,
      showNewComputer = showNewComputer,
      items = items,
      eventListener = {},
    )
  }
}

package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.burst.InterceptTest
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.domtester.SnapshotTester
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class ComputerTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  private var menuVisible by mutableStateOf(false)
  private var installAppDialogVisible by mutableStateOf(false)

  @Test
  fun happyPath() = runTest {
    snapshotTester.snapshot {
      Subject()
    }

    menuVisible = true
    snapshotTester.snapshot(name = "menuVisible") {
      Subject()
    }

    menuVisible = false
    installAppDialogVisible = true
    snapshotTester.snapshot(name = "installAppDialogVisible") {
      Subject()
    }
  }

  @Composable
  fun Subject() {
    Computer(
      scrimVisible = menuVisible || installAppDialogVisible,
      snapshot = ComputerSnapshot(
        slug = ComputerSlug("jesse99"),
        apps = listOf(
          app("Files"),
          app("Library"),
          app("Music"),
          app("Photos"),
          app("Pink Journal"),
          app("Recipes"),
          app("Smart Home"),
          app("Snake"),
          app("Writer"),
          app("Zap"),
        ),
      ),
      eventListener = {
      },
      overlays = { computerChildAttrs ->
        ComputerMenu(
          attrs = computerChildAttrs,
          visible = menuVisible,
          eventListener = {},
        )
        InstallAppDialog(
          attrs = computerChildAttrs,
          visible = installAppDialogVisible,
          eventListener = {},
        )
      },
    )
  }

  private fun app(launcherLabel: String) = InstalledAppSnapshot(
    slug = AppSlug("app"),
    launcherLabel = launcherLabel,
    maskableIconUrl = "/assets/launcher/sample-icon.svg",
    installScheduledAt = Instant.fromEpochSeconds(0L),
  )
}

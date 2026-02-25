package com.wasmo.client.app.invite

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class InviteTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun initial() = runTest {
    snapshotTester.snapshot {
      InviteScreen(
        inviteState = InviteState.ReadyToAccept,
      ) {
      }
    }
  }
}

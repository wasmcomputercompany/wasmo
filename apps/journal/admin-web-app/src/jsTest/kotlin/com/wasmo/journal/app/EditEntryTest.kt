package com.wasmo.journal.app

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class EditEntryTest {
  @InterceptTest
  val snapshotTester = SnapshotTester(
    stylesheetsUrls = listOf(
      "https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap",
      "/assets/Wasmo.css",
    ),
  )

  @Test
  fun happyPath() = runTest {
    val sample = SampleEntries.WasmIsLikeJson
    snapshotTester.snapshot {
      EditEntry(
        syncState = SyncState.Ready,
        title = sample.title,
        slug = sample.slug,
        visibility = sample.visibility,
        body = sample.body,
        eventListener = {},
      )
    }
  }
}

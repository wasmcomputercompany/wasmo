package com.wasmo.journal.app

import app.cash.burst.InterceptTest
import com.wasmo.domtester.SnapshotTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class EntryListTest {
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
      EntryList(
        entries = listOf(
          SampleEntries.WasmIsLikeJson.toSummary(),
          SampleEntries.MultipleColumnInClause.toSummary(),
          SampleEntries.WasmIsLikeJson.toSummary(),
          SampleEntries.MultipleColumnInClause.toSummary(),
        ),
        publishState = PublishStateViewModel(
          publishNeeded = true,
          publishRequested = false,
        ),
        eventListener = {},
      )
    }
  }
}

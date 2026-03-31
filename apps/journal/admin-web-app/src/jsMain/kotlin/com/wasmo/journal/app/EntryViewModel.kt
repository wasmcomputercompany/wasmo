package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.Visibility

data class EntryViewModel(
  val syncState: SyncState,
  val visibility: Visibility = Visibility.Private,
  val slug: String = "",
  val title: String = "",
  val body: String = "",
)

data class EntryListViewModel(
  val syncState: SyncState,
  val entries: List<EntrySummary>,
)

package com.wasmo.journal.app

import com.wasmo.journal.api.Visibility

data class EntryViewModel(
  val saveState: SaveState,
  val visibility: Visibility = Visibility.Private,
  val slug: String = "",
  val title: String = "",
  val body: String = "",
)

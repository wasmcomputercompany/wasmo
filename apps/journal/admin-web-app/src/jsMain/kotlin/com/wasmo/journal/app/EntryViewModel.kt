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

data class PublishStateViewModel(
  val publishNeeded: Boolean,
  val publishRequested: Boolean,
) {
  val canRequestPublish: Boolean
    get() = publishNeeded && !publishRequested
}

sealed interface UploadViewModel {
  data class Progress(
    val loaded: Double = 0.0,
    val total: Double = 1.0,
  ) : UploadViewModel

  data class Success(
    val url: String,
  ) : UploadViewModel

  data class Failed(
    val throwable: Throwable,
  ) : UploadViewModel
}

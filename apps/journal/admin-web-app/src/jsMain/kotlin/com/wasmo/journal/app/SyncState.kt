package com.wasmo.journal.app

sealed interface SyncState {
  data object Loading : SyncState
  data object Ready : SyncState
  data object Dirty : SyncState
  data class Error(val message: String) : SyncState
}

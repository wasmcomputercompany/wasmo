package com.wasmo.journal.app

sealed interface SaveState {
  data object Loading : SaveState
  data object Saved : SaveState
  data object Dirty : SaveState
  data class Error(val message: String) : SaveState
}

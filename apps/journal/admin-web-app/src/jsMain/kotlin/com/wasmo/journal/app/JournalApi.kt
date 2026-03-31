package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse

interface JournalApi {
  suspend fun listEntries(request: ListEntriesRequest): ListEntriesResponse
  suspend fun saveEntry(token: String, request: SaveEntryRequest): SaveEntryResponse
  suspend fun getEntry(token: String): EntrySnapshot
}

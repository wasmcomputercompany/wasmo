package com.wasmo.journal.app

import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.api.PublishState
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import org.w3c.files.File

interface JournalApi {
  suspend fun listEntries(request: ListEntriesRequest): ListEntriesResponse
  suspend fun saveEntry(token: String, request: SaveEntryRequest): SaveEntryResponse
  suspend fun getEntry(token: String): EntrySnapshot
  suspend fun addAttachment(
    entryToken: String,
    attachmentToken: String,
    file: File,
    onProgress: (loaded: Number, total: Number) -> Unit,
  )
  suspend fun getPublishState(): PublishState
  suspend fun requestPublish(): PublishState
}

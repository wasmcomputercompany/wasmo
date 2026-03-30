package com.wasmo.journal.server.admin

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.db.JournalDb

/**
 * ```
 * POST /api/entries
 * ```
 */
class ListEntriesAction(
  private val journalDb: JournalDb,
) {
  suspend fun list(request: ListEntriesRequest): ListEntriesResponse {
    val entries = journalDb.entryQueries.findEntries(limit = 10L).awaitAsList()
    return ListEntriesResponse(
      entries = entries.map {
        EntrySummary(
          token = it.token,
          visibility = it.visibility,
          slug = it.slug,
          title = it.title,
          date = it.date,
        )
      },
    )
  }

  companion object {
    val PathRegex = Regex("/api/entries")
  }
}

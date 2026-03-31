package com.wasmo.journal.server.admin

import com.wasmo.journal.api.SaveEntryError
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import com.wasmo.journal.db.JournalDb
import wasmo.sql.ConstraintViolationException

/**
 * ```
 * POST /api/entries/<token>
 * ```
 */
class SaveEntryAction(
  private val journalDb: JournalDb,
) {
  suspend fun save(
    entryToken: String,
    request: SaveEntryRequest,
  ): SaveEntryResponse {
    require(request.expectedVersion < request.entry.version) {
      "unexpected new version"
    }

    try {
      if (request.expectedVersion == 0L) {
        journalDb.entryQueries.insertEntry(
          token = entryToken,
          version = request.entry.version,
          visibility = request.entry.visibility,
          date = request.entry.date,
          slug = request.entry.slug,
          title = request.entry.title,
          body = request.entry.body,
        )
      } else {
        val rowCount = journalDb.entryQueries.saveEntry(
          new_version = request.entry.version,
          new_visibility = request.entry.visibility,
          new_date = request.entry.date,
          new_slug = request.entry.slug,
          new_title = request.entry.title,
          new_body = request.entry.body,
          expected_version = request.expectedVersion,
          token = entryToken,
        )
        check(rowCount == 1L)
      }
    } catch (e: ConstraintViolationException) {
      if (e.constraintName == "entry_slug_key") {
        return SaveEntryResponse(
          error = SaveEntryError.SlugConflict,
        )
      }
    }

    return SaveEntryResponse()
  }

  suspend fun save(
    match: MatchResult,
    request: SaveEntryRequest,
  ) = save(match.groups[1]!!.value, request)

  companion object {
    val PathRegex = Regex("/api/entries/([^/]+)")
  }
}

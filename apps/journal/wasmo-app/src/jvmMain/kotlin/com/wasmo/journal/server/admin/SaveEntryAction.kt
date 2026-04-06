package com.wasmo.journal.server.admin

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.wasmo.journal.api.SaveEntryError
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.db.JournalDb
import kotlin.time.Clock
import wasmo.sql.ConstraintViolationException

/**
 * ```
 * POST /api/entries/<token>
 * ```
 */
class SaveEntryAction(
  private val clock: Clock,
  private val journalDb: JournalDb,
) {
  suspend fun save(
    entryToken: String,
    request: SaveEntryRequest,
  ): SaveEntryResponse {
    try {
      val toUpdate = journalDb.entryQueries.findEntryByToken(entryToken)
        .awaitAsOneOrNull()
      val visibilityChanged = toUpdate == null || (request.entry.visibility != toUpdate.visibility)
      val syncNeededAt = when {
        visibilityChanged -> toUpdate?.sync_needed_at ?: clock.now()
        request.entry.visibility == Visibility.Published -> clock.now()
        else -> null
      }

      if (toUpdate == null) {
        journalDb.entryQueries.insertEntry(
          token = entryToken,
          version = 1L,
          sync_needed_at = syncNeededAt,
          visibility = request.entry.visibility,
          date = request.entry.date,
          slug = request.entry.slug,
          title = request.entry.title,
          body = request.entry.body,
        )
      } else {
        val rowCount = journalDb.entryQueries.saveEntry(
          new_version = toUpdate.version + 1,
          sync_needed_at = syncNeededAt,
          new_visibility = request.entry.visibility,
          new_date = request.entry.date,
          new_slug = request.entry.slug,
          new_title = request.entry.title,
          new_body = request.entry.body,
          expected_version = toUpdate.version,
          id = toUpdate.id,
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

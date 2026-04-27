package com.wasmo.journal.server.entries

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.wasmo.journal.api.SaveEntryError
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.db.JournalDb
import com.wasmo.journal.server.publishing.PublishTracker
import kotlin.time.Clock
import wasmo.sql.SqlException

/**
 * ```
 * POST /api/entries/<token>
 * ```
 */
class SaveEntryAction(
  private val clock: Clock,
  private val journalDb: JournalDb,
  private val publishTracker: PublishTracker,
) {
  suspend fun save(
    entryToken: String,
    request: SaveEntryRequest,
  ): SaveEntryResponse {
    val now = clock.now()
    try {
      val toUpdate = journalDb.entryQueries.findEntryByToken(entryToken)
        .awaitAsOneOrNull()
      val visibilityChanged = toUpdate == null || (request.entry.visibility != toUpdate.visibility)
      val publishNeededAt = when {
        visibilityChanged -> toUpdate?.publish_needed_at ?: now
        request.entry.visibility == Visibility.Published -> now
        else -> null
      }

      if (toUpdate == null) {
        journalDb.entryQueries.insertEntry(
          token = entryToken,
          version = 1L,
          publish_needed_at = publishNeededAt,
          visibility = request.entry.visibility,
          date = request.entry.date,
          slug = request.entry.slug,
          title = request.entry.title,
          body = request.entry.body,
        )
      } else {
        val rowCount = journalDb.entryQueries.saveEntry(
          new_version = toUpdate.version + 1,
          publish_needed_at = publishNeededAt,
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
    } catch (e: SqlException) {
      if (e.constraint == "entry_slug_key") {
        return SaveEntryResponse(
          error = SaveEntryError.SlugConflict,
        )
      }
    }

    publishTracker.setPublishNeeded(now)

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

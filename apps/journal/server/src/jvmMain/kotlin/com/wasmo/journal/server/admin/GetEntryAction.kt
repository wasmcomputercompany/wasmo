package com.wasmo.journal.server.admin

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import com.wasmo.journal.api.AttachmentSnapshot
import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.db.JournalDb

/**
 * ```
 * GET /api/entries/<token>
 * ```
 */
class GetEntryAction(
  private val journalDb: JournalDb,
) {
  suspend fun get(
    entryToken: String,
  ): EntrySnapshot {
    val entry = journalDb.entryQueries.findEntryByToken(entryToken).awaitAsOne()
    val attachments = journalDb.attachmentQueries
      .selectAttachmentsByEntryToken(entryToken, limit = 100)
      .awaitAsList()
    return EntrySnapshot(
      token = entry.token,
      version = entry.version,
      visibility = entry.visibility,
      slug = entry.slug,
      title = entry.title,
      date = entry.date,
      body = entry.body,
      attachments = attachments.map {
        AttachmentSnapshot(
          token = it.attachment_token,
        )
      },
    )
  }

  suspend fun get(
    match: MatchResult,
  ) = get(match.groups[1]!!.value)

  companion object {
    val PathRegex = Regex("/api/entries/([^/]+)")
  }
}

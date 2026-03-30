package com.wasmo.journal.server.attachments

import com.wasmo.journal.db.JournalDb
import wasmo.http.HttpResponse

/**
 * ```
 * GET /api/entries/<token>/attachments/<token>
 * ```
 */
class GetAttachmentAction(
  private val journalDb: JournalDb,
) {
  suspend fun save(
    entryToken: String,
    attachmentToken: String,
  ): HttpResponse {
    TODO()
  }
}

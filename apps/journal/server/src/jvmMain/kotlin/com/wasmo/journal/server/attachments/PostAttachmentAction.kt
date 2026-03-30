package com.wasmo.journal.server.attachments

import com.wasmo.journal.db.JournalDb
import okio.ByteString
import wasmo.http.HttpResponse

/**
 * ```
 * POST /api/entries/<token>/attachments/<token>
 * ```
 */
class PostAttachmentAction(
  private val journalDb: JournalDb,
) {
  suspend fun save(
    entryToken: String,
    attachmentToken: String,
    request: ByteString,
  ): HttpResponse {
    TODO()
  }
}

package com.wasmo.journal.server.attachments

import com.wasmo.journal.db.JournalDb
import kotlin.time.Clock
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.HttpResponse
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

/**
 * ```
 * POST /api/entries/<token>/attachments/<token>
 * ```
 */
class PostAttachmentAction(
  private val clock: Clock,
  private val objectStore: ObjectStore,
  private val journalDb: JournalDb,
) {
  suspend fun save(
    entryToken: String,
    attachmentToken: String,
    request: ByteString,
    contentType: String? = null,
  ): HttpResponse {
    objectStore.put(
      PutObjectRequest(
        key = "attachments/${entryToken}/${attachmentToken}",
        value = request,
        contentType = contentType,
      ),
    )

    journalDb.attachmentQueries.insertAttachment(
      entry_token = entryToken,
      attachment_token = attachmentToken,
      posted_at = clock.now(),
    )

    return HttpResponse(
      body = "{}".encodeUtf8(),
    )
  }
}

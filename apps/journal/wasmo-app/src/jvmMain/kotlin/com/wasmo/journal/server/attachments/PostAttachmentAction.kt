package com.wasmo.journal.server.attachments

import com.wasmo.journal.db.JournalDb
import kotlin.time.Clock
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.HttpRequest
import wasmo.http.HttpResponse

/**
 * ```
 * POST /api/entries/<token>/attachments/<token>
 * ```
 */
class PostAttachmentAction(
  private val clock: Clock,
  private val attachmentStore: AttachmentStore,
  private val journalDb: JournalDb,
) {
  suspend fun post(
    entryToken: String,
    attachmentToken: String,
    request: ByteString,
    contentType: String? = null,
  ): HttpResponse {
    // TODO: this should all be in an enclosing transaction.

    attachmentStore.put(
      entryToken = entryToken,
      attachmentToken = attachmentToken,
      attachment = Attachment(request, contentType),
    )

    journalDb.attachmentQueries.insertAttachment(
      entry_token = entryToken,
      attachment_token = attachmentToken,
      posted_at = clock.now(),
    )

    journalDb.entryQueries.setSyncNeededAt(
      sync_needed_at = clock.now(),
      token = entryToken,
    )

    return HttpResponse(
      body = "{}".encodeUtf8(),
    )
  }

  suspend fun post(
    match: MatchResult,
    request: HttpRequest,
  ) = post(
    entryToken = match.groups[1]!!.value,
    attachmentToken = match.groups[2]!!.value,
    request = request.body ?: ByteString.EMPTY,
    contentType = request.contentType,
  )

  companion object {
    val PathRegex = Regex("/api/entries/([^/]+)/attachments/([^/]+)")
  }
}

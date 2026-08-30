package com.wasmo.journal.server.attachments

import wasmo.http.HttpResponse
import wit.wasmo.http.Types.Header
import wit.wasmo.http.Types.HttpResponse

/**
 * ```
 * GET /api/entries/<token>/attachments/<token>
 * ```
 */
class GetAttachmentAction(
  private val attachmentStore: AttachmentStore,
) {
  suspend fun get(
    entryToken: String,
    attachmentToken: String,
  ): HttpResponse {
    val attachment = attachmentStore.get(entryToken, attachmentToken)
      ?: return HttpResponse(code = 404U)

    return HttpResponse(
      headers = buildList {
        attachment.contentType?.let { contentType ->
          add(Header("content-type", contentType))
        }
      },
      body = attachment.data,
    )
  }

  suspend fun get(
    match: MatchResult,
  ) = get(
    entryToken = match.groups[1]!!.value,
    attachmentToken = match.groups[2]!!.value,
  )

  companion object {
    val PathRegex = Regex("/api/entries/([^/]+)/attachments/([^/]+)")
  }
}

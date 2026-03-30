package com.wasmo.journal.server.attachments

import wasmo.http.Header
import wasmo.http.HttpResponse
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.ObjectStore

/**
 * ```
 * GET /api/entries/<token>/attachments/<token>
 * ```
 */
class GetAttachmentAction(
  private val objectStore: ObjectStore,
) {
  suspend fun get(
    entryToken: String,
    attachmentToken: String,
  ): HttpResponse {
    val response = objectStore.get(
      GetObjectRequest(
        key = "attachments/${entryToken}/${attachmentToken}",
      ),
    )

    val value = response.value
      ?: return HttpResponse(code = 404)

    val headers = mutableListOf<Header>()
    response.contentType?.let { contentType ->
      headers += Header("content-type", contentType)
    }

    return HttpResponse(
      headers = headers,
      body = value,
    )
  }
}

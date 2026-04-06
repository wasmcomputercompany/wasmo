package com.wasmo.journal.server.publishing

import com.wasmo.journal.db.Attachment
import com.wasmo.journal.server.attachments.AttachmentStore
import okio.ByteString.Companion.encodeUtf8
import wasmo.objectstore.DeleteObjectRequest
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

/**
 * Manages the `site/` directory in the object store.
 */
class PublishedSiteStore(
  private val objectStore: ObjectStore,
  private val attachmentStore: AttachmentStore,
) {
  suspend fun publishEntry(
    attachments: List<Attachment>,
    slug: String,
    html: String,
  ) {
    val sortedAttachments = attachments.sortedBy { it.posted_at }

    var fixedHtml = html
    for ((i, metadata) in sortedAttachments.withIndex()) {
      val attachmentPath = "$slug/a${i + 1}"
      val attachment = attachmentStore.get(
        attachmentToken = metadata.attachment_token,
        entryToken = metadata.entry_token,
      ) ?: continue

      fixedHtml = fixedHtml.replace(
        "\"/api/entries/${metadata.entry_token}/attachments/${metadata.attachment_token}\"",
        "\"/$attachmentPath\"",
      )

      objectStore.put(
        PutObjectRequest(
          key = "site/$attachmentPath",
          value = attachment.data,
          contentType = attachment.contentType,
        ),
      )
    }

    objectStore.put(
      PutObjectRequest(
        key = "site/$slug",
        value = fixedHtml.encodeUtf8(),
        contentType = "text/html; charset=utf-8",
      ),
    )
  }

  suspend fun unpublishEntry(
    attachments: List<Attachment>,
    slug: String,
  ) {
    for (i in 0 until attachments.size) {
      val attachmentPath = "$slug/a${i + 1}"
      objectStore.delete(
        DeleteObjectRequest(
          key = "site/$attachmentPath",
        ),
      )
    }

    objectStore.delete(
      DeleteObjectRequest(
        key = "site/$slug",
      ),
    )
  }
}

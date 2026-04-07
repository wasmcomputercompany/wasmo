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
class SiteStore(
  private val objectStore: ObjectStore,
  private val attachmentStore: AttachmentStore,
) {
  fun fixAttachmentsInHtml(
    attachments: List<Attachment>,
    slug: String,
    html: String,
  ): String {
    val sortedAttachments = attachments.sortedBy { it.posted_at }
    var result = html
    for ((i, metadata) in sortedAttachments.withIndex()) {
      result = result.replace(
        "\"/api/entries/${metadata.entry_token}/attachments/${metadata.attachment_token}\"",
        "\"/${attachmentPath(slug, i)}\"",
      )
    }
    return result
  }

  suspend fun publishList(
    html: String,
  ) {
    objectStore.put(
      PutObjectRequest(
        key = "site/entries/index",
        value = html.encodeUtf8(),
        contentType = "text/html; charset=utf-8",
      ),
    )
  }

  suspend fun deleteList() {
    objectStore.delete(
      DeleteObjectRequest(
        key = "site/entries/index",
      ),
    )
  }

  suspend fun publishEntry(
    attachments: List<Attachment>,
    slug: String,
    html: String,
  ) {
    val sortedAttachments = attachments.sortedBy { it.posted_at }

    for ((i, metadata) in sortedAttachments.withIndex()) {
      val attachment = attachmentStore.get(
        attachmentToken = metadata.attachment_token,
        entryToken = metadata.entry_token,
      ) ?: continue

      objectStore.put(
        PutObjectRequest(
          key = "site/attachments/${attachmentPath(slug, i)}",
          value = attachment.data,
          contentType = attachment.contentType,
        ),
      )
    }

    objectStore.put(
      PutObjectRequest(
        key = "site/entries/$slug",
        value = html.encodeUtf8(),
        contentType = "text/html; charset=utf-8",
      ),
    )
  }

  suspend fun unpublishEntry(
    attachments: List<Attachment>,
    slug: String,
  ) {
    for (i in 0 until attachments.size) {
      val attachmentPath = attachmentPath(slug, i)
      objectStore.delete(
        DeleteObjectRequest(
          key = "site/attachments/$attachmentPath",
        ),
      )
    }

    objectStore.delete(
      DeleteObjectRequest(
        key = "site/attachments/$slug",
      ),
    )
  }

  private fun attachmentPath(slug: String, index: Int): String = "$slug/a${index + 1}"
}

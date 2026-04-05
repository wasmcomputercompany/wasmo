package com.wasmo.journal.server.attachments

import okio.ByteString
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

class AttachmentStore(
  private val objectStore: ObjectStore,
) {
  suspend fun get(
    entryToken: String,
    attachmentToken: String,
  ): Attachment? {
    val response = objectStore.get(
      GetObjectRequest(
        key = "attachments/${entryToken}/${attachmentToken}",
      ),
    )

    return Attachment(
      data = response.value ?: return null,
      contentType = response.contentType,
    )
  }

  suspend fun put(
    entryToken: String,
    attachmentToken: String,
    attachment: Attachment,
  ) {
    objectStore.put(
      PutObjectRequest(
        key = "attachments/${entryToken}/${attachmentToken}",
        value = attachment.data,
        contentType = attachment.contentType,
      ),
    )
  }
}

data class Attachment(
  val data: ByteString,
  val contentType: String? = null,
)

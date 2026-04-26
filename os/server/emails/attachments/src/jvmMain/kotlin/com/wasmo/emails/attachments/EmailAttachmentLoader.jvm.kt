package com.wasmo.emails.attachments

import com.wasmo.sendemail.EmailAttachment
import okio.FileSystem
import okio.Path.Companion.toPath

internal actual val PlatformEmailAttachmentLoader = object : EmailAttachmentLoader {
  override fun load(
    fileName: String,
    contentType: String,
  ) = EmailAttachment(
    fileName = fileName,
    contentType = contentType,
    url = "cid:$fileName",
    content = FileSystem.RESOURCES.read("/static/assets/emails/$fileName".toPath()) {
      readByteString()
    },
  )
}

package com.wasmo.emails.attachments

import com.wasmo.sendemail.EmailAttachment
import okio.ByteString

internal actual val PlatformEmailAttachmentLoader = object : EmailAttachmentLoader {
  override fun load(
    fileName: String,
    contentType: String,
  ) = EmailAttachment(
    fileName = fileName,
    contentType = contentType,
    url = "/assets/emails/$fileName",
    content = ByteString.EMPTY,
  )
}

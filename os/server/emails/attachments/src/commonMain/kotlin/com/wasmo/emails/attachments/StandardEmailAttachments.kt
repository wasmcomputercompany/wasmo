package com.wasmo.emails.attachments

import com.wasmo.sendemail.EmailAttachment

object StandardEmailAttachments {
  val wordmark512x160: EmailAttachment =
    PlatformEmailAttachmentLoader.load(
      fileName = "wordmark512x160.png",
      contentType = "image/png",
    )
}

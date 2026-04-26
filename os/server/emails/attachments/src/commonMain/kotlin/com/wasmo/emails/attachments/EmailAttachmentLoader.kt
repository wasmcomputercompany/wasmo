package com.wasmo.emails.attachments

import com.wasmo.sendemail.EmailAttachment

internal interface EmailAttachmentLoader {
  fun load(
    fileName: String,
    contentType: String,
  ): EmailAttachment
}

internal expect val PlatformEmailAttachmentLoader: EmailAttachmentLoader

package com.wasmo.sendemail

import okio.ByteString

interface SendEmailService {
  /**
   * @throws EmailSendFailedException
   */
  suspend fun send(message: EmailMessage)
}

data class EmailMessage(
  val from: String,
  val to: String,
  val subject: String,
  val html: String,
  val attachments: List<EmailAttachment>,
)

data class EmailAttachment(
  val fileName: String,
  val contentType: String,
  val url: String,
  val content: ByteString,
)

class EmailSendFailedException(message: String) : Exception(message)

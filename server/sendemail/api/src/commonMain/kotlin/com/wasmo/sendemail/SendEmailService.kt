package com.wasmo.sendemail

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
)

class EmailSendFailedException(message: String) : Exception(message)

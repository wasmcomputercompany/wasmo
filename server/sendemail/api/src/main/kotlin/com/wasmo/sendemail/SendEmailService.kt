package com.wasmo.sendemail

interface SendEmailService {
  /**
   * @throws EmailSendFailedException
   */
  suspend fun sendEmail(
    from: String,
    to: String,
    subject: String,
    htmlBody: String,
  )
}

class EmailSendFailedException(message: String) : Exception(message)

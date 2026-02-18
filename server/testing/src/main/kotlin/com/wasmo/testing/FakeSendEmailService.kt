package com.wasmo.testing

import com.wasmo.sendemail.EmailMessage
import com.wasmo.sendemail.SendEmailService

class FakeSendEmailService : SendEmailService {
  val emails = ArrayDeque<EmailMessage>()

  override suspend fun send(message: EmailMessage) {
    emails += message
  }

  fun takeEmail(): EmailMessage {
    return emails.removeFirst()
  }
}

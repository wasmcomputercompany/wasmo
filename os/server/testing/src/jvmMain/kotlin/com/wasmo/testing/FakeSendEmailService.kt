package com.wasmo.testing

import com.wasmo.identifiers.OsScope
import com.wasmo.sendemail.EmailMessage
import com.wasmo.sendemail.SendEmailService
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class FakeSendEmailService : SendEmailService {
  val emails = ArrayDeque<EmailMessage>()

  override suspend fun send(message: EmailMessage) {
    emails += message
  }

  fun takeEmail(): EmailMessage {
    return emails.removeFirst()
  }
}

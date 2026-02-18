package com.wasmo.sendemail.postmark

import com.wasmo.sendemail.EmailMessage
import com.wasmo.sendemail.EmailSendFailedException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient

class PostmarkConnectivityTest {
  @Test
  fun happyPath() = runTest {
    val emailService = createEmailService() ?: return@runTest

    emailService.send(
      message = EmailMessage(
        from = "noreply@wasmo.com",
        to = "jesse@wasmo.com",
        subject = "PostmarkConnectivityTest",
        html = """<h1>Hello</h1> from PostmarkConnectivityTest""",
      ),
    )
  }

  @Test
  fun badFromAddress() = runTest {
    val emailService = createEmailService() ?: return@runTest

    assertFailsWith<EmailSendFailedException> {
      emailService.send(
        message = EmailMessage(
          from = "jesse@example.com",
          to = "jesse@wasmo.com",
          subject = "PostmarkConnectivityTest",
          html = """<h1>Hello</h1> from PostmarkConnectivityTest""",
        ),
      )
    }
  }

  private fun createEmailService(): PostmarkEmailService? {
    val credentials = PostmarkCredentials(
      baseUrl = PostmarkProductionBaseUrl,
      serverToken = System.getenv("POSTMARK_SERVER_TOKEN") ?: return null,
    )

    return PostmarkEmailService.Factory(
      credentials = credentials,
      client = OkHttpClient(),
    ).create()
  }
}

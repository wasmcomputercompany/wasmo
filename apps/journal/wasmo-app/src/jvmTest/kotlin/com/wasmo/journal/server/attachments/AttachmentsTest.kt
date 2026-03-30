package com.wasmo.journal.server.attachments

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.journal.server.JournalAppTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.Header
import wasmo.http.HttpResponse

class AttachmentsTest {
  @InterceptTest
  val tester = JournalAppTester()

  @Test
  fun postAndGetAttachment() = runTest {
    val entryToken = "aaaaabbbbbcccccdddddeeeee"
    val attachmentToken = "fffffggggghhhhhiiiiijjjjj"
    assertThat(
      tester.app.postAttachmentAction().save(
        entryToken = entryToken,
        attachmentToken = attachmentToken,
        request = "this is an attachment!".encodeUtf8(),
        contentType = "text/plain",
      ),
    ).isEqualTo(
      HttpResponse(
        body = "{}".encodeUtf8(),
      ),
    )

    assertThat(
      tester.app.getAttachmentAction().get(
        entryToken = entryToken,
        attachmentToken = attachmentToken,
      ),
    ).isEqualTo(
      HttpResponse(
        headers = listOf(
          Header("content-type", "text/plain"),
        ),
        body = "this is an attachment!".encodeUtf8(),
      ),
    )
  }

  @Test
  fun contentTypeAbsent() = runTest {
    val entryToken = "aaaaabbbbbcccccdddddeeeee"
    val attachmentToken = "fffffggggghhhhhiiiiijjjjj"
    tester.app.postAttachmentAction().save(
      entryToken = entryToken,
      attachmentToken = attachmentToken,
      request = "this is an attachment!".encodeUtf8(),
    )

    assertThat(
      tester.app.getAttachmentAction().get(
        entryToken = entryToken,
        attachmentToken = attachmentToken,
      ),
    ).isEqualTo(
      HttpResponse(
        body = "this is an attachment!".encodeUtf8(),
      ),
    )
  }

  @Test
  fun attachmentNotFound() = runTest {
    val entryToken = "aaaaabbbbbcccccdddddeeeee"
    val attachmentToken = "fffffggggghhhhhiiiiijjjjj"

    assertThat(
      tester.app.getAttachmentAction().get(
        entryToken = entryToken,
        attachmentToken = attachmentToken,
      ),
    ).isEqualTo(
      HttpResponse(
        code = 404,
      ),
    )
  }
}

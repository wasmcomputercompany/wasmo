package com.wasmo.journal.server.publishing

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.server.JournalAppTester
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

class PublishingTest {
  @InterceptTest
  val tester = JournalAppTester()

  private val entryToken = "aaaaabbbbbcccccdddddeeeee"
  private val attachment1 = "fffffggggghhhhhiiiiijjjjj"
  private val entryData = EntrySnapshot(
    token = entryToken,
    visibility = Visibility.Published,
    slug = "publish-this",
    title = "this post will be published!",
    date = Instant.fromEpochSeconds(0L),
    body = """
      |This post has one attachment:
      |<img src="/api/entries/$entryToken/attachments/$attachment1">
      """.trimMargin(),
  )
  private val attachmentData = "this is an attachment!".encodeUtf8()
  private val renderedList = """
    |<html>
    |  <head>
    |    <meta charset="utf-8">
    |    <title>Journal</title>
    |  </head>
    |  <body>
    |    <h1>this post will be published!</h1>
    |    <p>This post has one attachment:
    |<img src="/publish-this/a1"></p>
    |  </body>
    |</html>
    |
    """.trimMargin()
  private val renderedEntry = """
    |<html>
    |  <head>
    |    <meta charset="utf-8">
    |    <title>this post will be published!</title>
    |  </head>
    |  <body>
    |    <h1>this post will be published!</h1>
    |    <p>This post has one attachment:
    |<img src="/publish-this/a1"></p>
    |  </body>
    |</html>
    |
    """.trimMargin()

  @Test
  fun publishPost() = runTest {
    tester.httpService.postAttachmentAction().post(
      entryToken = entryToken,
      attachmentToken = attachment1,
      request = attachmentData,
    )

    tester.httpService.saveEntryAction().save(
      entryToken = entryToken,
      request = SaveEntryRequest(entryData),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore["site/index"]?.utf8())
      .isEqualTo(renderedList)
    assertThat(tester.platform.objectStore["site/publish-this"]?.utf8())
      .isEqualTo(renderedEntry)
    assertThat(tester.platform.objectStore["site/publish-this/a1"]?.utf8())
      .isEqualTo("this is an attachment!")
  }

  @Test
  fun privatePostIsNotPublished() = runTest {
    tester.httpService.postAttachmentAction().post(
      entryToken = entryToken,
      attachmentToken = attachment1,
      request = attachmentData,
    )
    tester.httpService.saveEntryAction().save(
      entryToken = entryToken,
      request = SaveEntryRequest(
        entryData.copy(
          visibility = Visibility.Private,
        )
      ),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore.list("site/")).isEmpty()
  }

  @Test
  fun makingPublicPostPrivateHidesIt() = runTest {
    tester.httpService.postAttachmentAction().post(
      entryToken = entryToken,
      attachmentToken = attachment1,
      request = attachmentData,
    )
    tester.httpService.saveEntryAction().save(
      entryToken = entryToken,
      request = SaveEntryRequest(entryData),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore.list("site/")).isNotEmpty()

    tester.httpService.saveEntryAction().save(
      entryToken = entryToken,
      request = SaveEntryRequest(
        entryData.copy(
          visibility = Visibility.Private,
        )
      ),
    )
    tester.sitePublisher.publishSite()
    assertThat(tester.platform.objectStore.list("site/")).isEmpty()
  }
}

package com.wasmo.journal.server.publishing

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.PublishState
import com.wasmo.journal.api.RequestPublishRequest
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.server.JournalAppTester
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

class PublishingTest {
  @InterceptTest
  val tester = JournalAppTester()

  private val entry1 = "aaaaabbbbbcccccdddddeeeee"
  private val entry1Attachment1 = "fffffggggghhhhhiiiiijjjjj"
  private val entry1Data = EntrySnapshot(
    token = entry1,
    visibility = Visibility.Published,
    slug = "publish-this",
    title = "this post will be published!",
    date = Instant.fromEpochSeconds(0L),
    body = """
      |This post has one attachment:
      |<img src="/api/entries/$entry1/attachments/$entry1Attachment1">
      """.trimMargin(),
  )
  private val entry1Attachment1Data = "this is an attachment!".encodeUtf8()
  private val renderedEntry1 = """
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

  private val entry2 = "kkkkklllllmmmmmnnnnnooooo"
  private val entry2Data = EntrySnapshot(
    token = entry2,
    visibility = Visibility.Published,
    slug = "second-post",
    title = "the newest post is listed first",
    date = Instant.fromEpochSeconds(1L),
    body = "This is the second post",
  )
  private val renderedEntry2 = """
    |<html>
    |  <head>
    |    <meta charset="utf-8">
    |    <title>the newest post is listed first</title>
    |  </head>
    |  <body>
    |    <h1>the newest post is listed first</h1>
    |    <p>This is the second post</p>
    |  </body>
    |</html>
    |
    """.trimMargin()

  private val renderedListEntry1 = """
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

  private val renderedListEntry2Entry1 = """
    |<html>
    |  <head>
    |    <meta charset="utf-8">
    |    <title>Journal</title>
    |  </head>
    |  <body>
    |    <h1>the newest post is listed first</h1>
    |    <p>This is the second post</p>
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
      entryToken = entry1,
      attachmentToken = entry1Attachment1,
      request = entry1Attachment1Data,
    )

    tester.httpService.saveEntryAction().save(
      entryToken = entry1,
      request = SaveEntryRequest(entry1Data),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore["site/entries/index"]?.utf8())
      .isEqualTo(renderedListEntry1)
    assertThat(tester.platform.objectStore["site/entries/publish-this"]?.utf8())
      .isEqualTo(renderedEntry1)
    assertThat(tester.platform.objectStore["site/attachments/publish-this/a1"]?.utf8())
      .isEqualTo("this is an attachment!")
  }

  @Test
  fun publishState() = runTest {
    val startAt = tester.clock.now()
    assertThat(tester.httpService.getPublishStateAction().get()).isEqualTo(
      PublishState(
        publishNeededAt = null,
        lastPublishedAt = startAt,
      ),
    )

    val entry1PostedAt = tester.clock.tick()
    tester.httpService.saveEntryAction().save(
      entryToken = entry1,
      request = SaveEntryRequest(entry1Data),
    )
    assertThat(tester.httpService.getPublishStateAction().get()).isEqualTo(
      PublishState(
        publishNeededAt = entry1PostedAt,
        lastPublishedAt = startAt,
      ),
    )

    val sitePublishedAt = tester.clock.tick()
    tester.sitePublisher.publishSite()
    assertThat(tester.httpService.getPublishStateAction().get()).isEqualTo(
      PublishState(
        publishNeededAt = null,
        lastPublishedAt = sitePublishedAt,
      ),
    )
  }

  @Test
  @Ignore("job queues aren't testable yet by app code")
  fun publishApi() = runTest {
    tester.httpService.saveEntryAction().save(
      entryToken = entry2,
      request = SaveEntryRequest(entry2Data),
    )
    tester.httpService.requestPublishAction().requestPublish(RequestPublishRequest)
    tester.platform.jobQueueFactory.awaitIdle()

    assertThat(tester.platform.objectStore["site/entries/second-post"]?.utf8())
      .isEqualTo(renderedEntry2)
  }

  @Test
  fun publishMultiplePosts() = runTest {
    tester.httpService.postAttachmentAction().post(
      entryToken = entry1,
      attachmentToken = entry1Attachment1,
      request = entry1Attachment1Data,
    )
    tester.httpService.saveEntryAction().save(
      entryToken = entry1,
      request = SaveEntryRequest(entry1Data),
    )
    tester.httpService.saveEntryAction().save(
      entryToken = entry2,
      request = SaveEntryRequest(entry2Data),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore["site/entries/index"]?.utf8())
      .isEqualTo(renderedListEntry2Entry1)
    assertThat(tester.platform.objectStore["site/entries/publish-this"]?.utf8())
      .isEqualTo(renderedEntry1)
    assertThat(tester.platform.objectStore["site/entries/second-post"]?.utf8())
      .isEqualTo(renderedEntry2)
    assertThat(tester.platform.objectStore["site/attachments/publish-this/a1"]?.utf8())
      .isEqualTo("this is an attachment!")
  }

  @Test
  fun privatePostIsNotPublished() = runTest {
    tester.httpService.postAttachmentAction().post(
      entryToken = entry1,
      attachmentToken = entry1Attachment1,
      request = entry1Attachment1Data,
    )
    tester.httpService.saveEntryAction().save(
      entryToken = entry1,
      request = SaveEntryRequest(
        entry1Data.copy(
          visibility = Visibility.Private,
        ),
      ),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore.list("site/")).isEmpty()
  }

  @Test
  fun makingPublicPostPrivateHidesIt() = runTest {
    tester.httpService.postAttachmentAction().post(
      entryToken = entry1,
      attachmentToken = entry1Attachment1,
      request = entry1Attachment1Data,
    )
    tester.httpService.saveEntryAction().save(
      entryToken = entry1,
      request = SaveEntryRequest(entry1Data),
    )
    tester.sitePublisher.publishSite()

    assertThat(tester.platform.objectStore.list("site/")).isNotEmpty()

    tester.httpService.saveEntryAction().save(
      entryToken = entry1,
      request = SaveEntryRequest(
        entry1Data.copy(
          visibility = Visibility.Private,
        ),
      ),
    )
    tester.sitePublisher.publishSite()
    assertThat(tester.platform.objectStore.list("site/")).isEmpty()
  }
}

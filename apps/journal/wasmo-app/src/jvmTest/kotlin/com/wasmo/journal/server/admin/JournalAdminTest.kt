package com.wasmo.journal.server.admin

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.journal.api.AttachmentSnapshot
import com.wasmo.journal.api.EntrySnapshot
import com.wasmo.journal.api.EntrySummary
import com.wasmo.journal.api.ListEntriesRequest
import com.wasmo.journal.api.ListEntriesResponse
import com.wasmo.journal.api.SaveEntryRequest
import com.wasmo.journal.api.SaveEntryResponse
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.server.JournalAppTester
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

class JournalAdminTest {
  @InterceptTest
  val tester = JournalAppTester()

  @Test
  fun saveNewEntry() = runTest {
    assertThat(
      tester.app.listEntriesAction().list(
        ListEntriesRequest(),
      ),
    ).isEqualTo(
      ListEntriesResponse(
        entries = listOf(),
      ),
    )

    val entryToken = "aaaaabbbbbcccccdddddeeeee"
    assertThat(
      tester.app.saveEntryAction().save(
        entryToken,
        SaveEntryRequest(
          expectedVersion = 0L,
          entry = EntrySnapshot(
            token = entryToken,
            version = 1L,
            visibility = Visibility.Private,
            slug = "hello-world",
            title = "Hello, Journal",
            date = Instant.fromEpochSeconds(123L),
            body = "This is my first journal entry!",
          ),
        ),
      ),
    ).isEqualTo(
      SaveEntryResponse(),
    )

    assertThat(
      tester.app.listEntriesAction().list(
        ListEntriesRequest(),
      ),
    ).isEqualTo(
      ListEntriesResponse(
        entries = listOf(
          EntrySummary(
            token = entryToken,
            visibility = Visibility.Private,
            slug = "hello-world",
            title = "Hello, Journal",
            date = Instant.fromEpochSeconds(123L),
          ),
        ),
      ),
    )


    assertThat(
      tester.app.getEntryAction().get(
        entryToken = entryToken,
      ),
    ).isEqualTo(
      EntrySnapshot(
        token = entryToken,
        version = 1L,
        visibility = Visibility.Private,
        slug = "hello-world",
        title = "Hello, Journal",
        date = Instant.fromEpochSeconds(123L),
        body = "This is my first journal entry!",
      ),
    )
  }

  @Test
  fun entryHasAttachments() = runTest {
    val entryToken = "aaaaabbbbbcccccdddddeeeee"
    val attachment1 = "fffffggggghhhhhiiiiijjjjj"
    val attachment2 = "kkkkklllllmmmmmnnnnnooooo"
    tester.app.postAttachmentAction().post(
      entryToken = entryToken,
      attachmentToken = attachment1,
      request = "this is an attachment!".encodeUtf8(),
    )
    tester.app.postAttachmentAction().post(
      entryToken = entryToken,
      attachmentToken = attachment2,
      request = "this is another attachment!".encodeUtf8(),
    )

    tester.app.saveEntryAction().save(
      entryToken,
      SaveEntryRequest(
        expectedVersion = 0L,
        entry = EntrySnapshot(
          token = entryToken,
          version = 1L,
          visibility = Visibility.Private,
          slug = "has-attachments",
          title = "This post has attachments",
          date = Instant.fromEpochSeconds(0L),
          body = "two of 'em",
        ),
      ),
    )

    assertThat(
      tester.app.getEntryAction().get(
        entryToken = entryToken,
      ),
    ).isEqualTo(
      EntrySnapshot(
        token = entryToken,
        version = 1L,
        visibility = Visibility.Private,
        slug = "has-attachments",
        title = "This post has attachments",
        date = Instant.fromEpochSeconds(0L),
        body = "two of 'em",
        attachments = listOf(
          AttachmentSnapshot(attachment1),
          AttachmentSnapshot(attachment2),
        ),
      ),
    )
  }

  @Test
  fun updateEntry() = runTest {
    val entryToken = "aaaaabbbbbcccccdddddeeeee"
    assertThat(
      tester.app.saveEntryAction().save(
        entryToken,
        SaveEntryRequest(
          expectedVersion = 0L,
          entry = EntrySnapshot(
            token = entryToken,
            version = 1L,
            visibility = Visibility.Private,
            slug = "hello-world",
            title = "Hello, Journal",
            date = Instant.fromEpochSeconds(123L),
            body = "This is my first journal entry!",
          ),
        ),
      ),
    ).isEqualTo(
      SaveEntryResponse(),
    )

    assertThat(
      tester.app.saveEntryAction().save(
        entryToken,
        SaveEntryRequest(
          expectedVersion = 1L,
          entry = EntrySnapshot(
            token = entryToken,
            version = 2L,
            visibility = Visibility.Private,
            slug = "hello-world-v2",
            title = "Hello, Journal, version 2",
            date = Instant.fromEpochSeconds(456L),
            body = "This is the second version of my first journal entry!",
          ),
        ),
      ),
    ).isEqualTo(
      SaveEntryResponse(),
    )

    assertThat(
      tester.app.getEntryAction().get(
        entryToken = entryToken,
      ),
    ).isEqualTo(
      EntrySnapshot(
        token = entryToken,
        version = 2L,
        visibility = Visibility.Private,
        slug = "hello-world-v2",
        title = "Hello, Journal, version 2",
        date = Instant.fromEpochSeconds(456L),
        body = "This is the second version of my first journal entry!",
      ),
    )
  }
}

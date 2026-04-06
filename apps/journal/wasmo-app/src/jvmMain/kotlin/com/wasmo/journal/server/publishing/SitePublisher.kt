package com.wasmo.journal.server.publishing

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.db.Entry
import com.wasmo.journal.db.JournalDb
import okio.Buffer
import okio.ByteString
import wasmo.jobs.JobHandler

class SitePublisher(
  private val siteStore: SiteStore,
  private val journalDb: JournalDb,
  private val siteRenderer: SiteRenderer,
) : JobHandler {
  override suspend fun handle(job: ByteString) {
    publishSite()
  }

  suspend fun publishSite() {
    publishList()

    val entriesToSync = journalDb.entryQueries.findEntriesToSync(
      limit = 100,
      mapper = ::Entry,
    ).awaitAsList()

    for (entry in entriesToSync) {
      publishEntry(entry)
    }
  }

  private suspend fun publishList() {
    val publishedEntries = journalDb.entryQueries.findEntriesWithVisibility(
      visibility = Visibility.Published,
      limit = 100,
    ).awaitAsList()

    if (publishedEntries.isEmpty()) {
      siteStore.deleteList()
      return
    }

    var html = run {
      val buffer = Buffer()
      context(buffer) {
        siteRenderer.renderEntryList(publishedEntries)
      }
      buffer.readUtf8()
    }

    for (entry in publishedEntries) {
      val attachments = journalDb.attachmentQueries.selectAttachmentsByEntryToken(
        entry_token = entry.token,
        limit = 1000,
      ).awaitAsList()

      html = siteStore.fixAttachmentsInHtml(
        attachments = attachments,
        slug = entry.slug,
        html = html,
      )
    }

    siteStore.publishList(
      html = html,
    )
  }

  private suspend fun publishEntry(entry: Entry) {
    val attachments = journalDb.attachmentQueries.selectAttachmentsByEntryToken(
      entry_token = entry.token,
      limit = 1000,
    ).awaitAsList()

    if (entry.visibility != Visibility.Published) {
      siteStore.unpublishEntry(
        attachments = attachments,
        slug = entry.slug,
      )
      return
    }

    var html = run {
      val buffer = Buffer()
      context(buffer) {
        siteRenderer.renderEntryToHtml(entry)
      }
      buffer.readUtf8()
    }

    html = siteStore.fixAttachmentsInHtml(
      attachments = attachments,
      slug = entry.slug,
      html = html,
    )

    siteStore.publishEntry(
      attachments = attachments,
      slug = entry.slug,
      html = html,
    )

    journalDb.entryQueries.clearSyncNeededAt(
      new_version = entry.version + 1,
      sync_needed_at = null,
      expected_version = entry.version,
      id = entry.id,
    )
  }

  companion object {
    val QueueName = "publish-site"
  }
}

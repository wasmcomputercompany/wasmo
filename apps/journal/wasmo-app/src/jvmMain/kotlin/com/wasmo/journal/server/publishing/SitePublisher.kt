package com.wasmo.journal.server.publishing

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.db.Entry
import com.wasmo.journal.db.JournalDb
import okio.ByteString
import wasmo.jobs.JobHandler

class SitePublisher(
  private val publishedSiteStore: PublishedSiteStore,
  private val journalDb: JournalDb,
  private val siteRenderer: SiteRenderer,
) : JobHandler {
  override suspend fun handle(job: ByteString) {
    publishSite()
  }

  suspend fun publishSite() {
    val entriesToSync = journalDb.entryQueries.findEntriesToSync(
      limit = 100,
      mapper = ::Entry,
    ).awaitAsList()

    for (entry in entriesToSync) {
      val attachments = journalDb.attachmentQueries.selectAttachmentsByEntryToken(
        entry_token = entry.token,
        limit = 1000,
      ).awaitAsList()

      if (entry.visibility == Visibility.Published) {
        publishedSiteStore.publishEntry(
          attachments = attachments,
          slug = entry.slug,
          html = siteRenderer.renderEntryToHtml(entry),
        )
      } else {
        publishedSiteStore.unpublishEntry(
          attachments = attachments,
          slug = entry.slug,
        )
      }

      journalDb.entryQueries.clearSyncNeededAt(
        new_version = entry.version + 1,
        sync_needed_at = null,
        expected_version = entry.version,
        id = entry.id,
      )
    }
  }

  companion object {
    val QueueName = "publish-site"
  }
}

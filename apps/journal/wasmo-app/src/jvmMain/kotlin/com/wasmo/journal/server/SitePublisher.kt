package com.wasmo.journal.server

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.wasmo.journal.api.Visibility
import com.wasmo.journal.db.Entry
import com.wasmo.journal.db.JournalDb
import com.wasmo.support.okiohtml.writeHtml
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.title
import okio.Buffer
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

class SitePublisher(
  private val objectStore: ObjectStore,
  private val journalDb: JournalDb,
) {
  suspend fun publishSite() {
    val entries = journalDb.entryQueries.findEntriesByVisibility(
      visibility = Visibility.Published,
      limit = 100,
    ).awaitAsList()

    for (entry in entries) {
      publishEntryHtml(entry)
    }
  }

  suspend fun publishEntryHtml(entry: Entry) {
    val buffer = Buffer()
    buffer.writeHtml {
      head {
        meta(charset = "utf-8")
        title(entry.title)
      }
      body {
        h1 {
          text(entry.title)
        }
        p {
          text(entry.body)
        }
      }
    }

    objectStore.put(
      PutObjectRequest(
        key = "site/${entry.slug}",
        value = buffer.readByteString(),
        contentType = "text/html; charset=utf-8",
      ),
    )
  }
}

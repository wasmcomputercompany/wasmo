package com.wasmo.journal.server

import com.wasmo.journal.db.JournalDbService
import com.wasmo.journal.server.attachments.AttachmentStore
import com.wasmo.journal.server.publishing.PublishedSiteStore
import com.wasmo.journal.server.publishing.SitePublisher
import com.wasmo.journal.server.publishing.SiteRenderer
import com.wasmo.sqldelight.driver
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp

class JournalWasmoApp(
  private val journalDb: JournalDbService,
  override val httpService: JournalHttpService,
  override val jobHandlerFactory: JournalJobHandlerFactory,
) : Closeable, WasmoApp() {
  override suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    journalDb.migrate()
  }

  override fun close() {
    journalDb.close()
  }

  class Factory(
    private val prettyPrint: Boolean = false,
  ) : WasmoApp.Factory {
    override suspend fun create(platform: Platform): JournalWasmoApp {
      val clock = platform.clock
      val journalDb = JournalDbService(
        driver = platform.sqlService.getOrCreate().driver(),
      )
      val attachmentStore = AttachmentStore(
        objectStore = platform.objectStore,
      )
      val publishedSiteStore = PublishedSiteStore(
        objectStore = platform.objectStore,
        attachmentStore = attachmentStore,
      )
      val sitePublisher = SitePublisher(
        siteRenderer = SiteRenderer(
          prettyPrint = prettyPrint,
        ),
        publishedSiteStore = publishedSiteStore,
        journalDb = journalDb,
      )
      val httpService = JournalHttpService(
        clock = clock,
        attachmentStore = attachmentStore,
        journalDb = journalDb,
      )
      val jobHandlerFactory = JournalJobHandlerFactory(
        sitePublisher = sitePublisher,
      )
      return JournalWasmoApp(
        journalDb = journalDb,
        httpService = httpService,
        jobHandlerFactory = jobHandlerFactory,
      )
    }
  }
}

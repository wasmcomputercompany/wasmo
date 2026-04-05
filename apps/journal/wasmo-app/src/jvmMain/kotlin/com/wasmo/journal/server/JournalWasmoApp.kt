package com.wasmo.journal.server

import com.wasmo.journal.db.JournalDbService
import com.wasmo.journal.server.attachments.AttachmentStore
import com.wasmo.sqldelight.driver
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp
import wasmo.jobs.JobHandler

class JournalWasmoApp(
  private val journalDb: JournalDbService,
  override val httpService: JournalHttpService,
  override val jobHandlerFactory: JobHandler.Factory,
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

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform): JournalWasmoApp {
      val clock = platform.clock
      val journalDb = JournalDbService(
        driver = platform.sqlService.getOrCreate().driver(),
      )
      val attachmentStore = AttachmentStore(
        objectStore = platform.objectStore,
      )
      val sitePublisher = SitePublisher(
        objectStore = platform.objectStore,
        journalDb = journalDb,
      )
      val httpService = JournalHttpService(
        clock = clock,
        attachmentStore = attachmentStore,
        journalDb = journalDb,
      )
      val jobHandlerFactory = JournalJobHandlerFactory(
        publishSiteJobHandler = PublishSiteJobHandler(
          sitePublisher = sitePublisher,
        ),
      )
      return JournalWasmoApp(
        journalDb = journalDb,
        httpService = httpService,
        jobHandlerFactory = jobHandlerFactory,
      )
    }
  }
}

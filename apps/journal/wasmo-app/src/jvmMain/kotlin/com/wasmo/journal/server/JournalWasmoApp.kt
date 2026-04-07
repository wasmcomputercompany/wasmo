package com.wasmo.journal.server

import com.wasmo.journal.db.JournalDb
import com.wasmo.journal.db.JournalDbService
import com.wasmo.journal.server.attachments.AttachmentStore
import com.wasmo.journal.server.publishing.PublishTracker
import com.wasmo.journal.server.publishing.SitePublisher
import com.wasmo.journal.server.publishing.SiteRenderer
import com.wasmo.journal.server.publishing.SiteStore
import com.wasmo.sqldelight.driver
import okio.Closeable
import wasmo.app.Platform
import wasmo.app.WasmoApp

class JournalWasmoApp(
  private val journalDb: JournalDbService,
  private val publishTracker: PublishTracker,
  override val httpService: JournalHttpService,
  override val jobHandlerFactory: JournalJobHandlerFactory,
) : Closeable, WasmoApp() {
  override suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
    journalDb.migrate(
      oldVersion = appVersionToSchemaVersion(oldVersion),
      newVersion = appVersionToSchemaVersion(newVersion),
    )

    publishTracker.migrate(oldVersion, newVersion)
  }

  private fun appVersionToSchemaVersion(appVersion: Long): Long {
    return when (appVersion) {
      0L -> 0L
      1L -> JournalDb.Schema.version
      else -> error("unexpected app version: $appVersion")
    }
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
        clock = clock,
        driver = platform.sqlService.getOrCreate().driver(),
      )
      val attachmentStore = AttachmentStore(
        objectStore = platform.objectStore,
      )
      val siteStore = SiteStore(
        objectStore = platform.objectStore,
        attachmentStore = attachmentStore,
      )
      val publishTracker = PublishTracker(
        clock = clock,
        journalDb = journalDb,
      )
      val sitePublisher = SitePublisher(
        clock = clock,
        siteRenderer = SiteRenderer(
          prettyPrint = prettyPrint,
        ),
        siteStore = siteStore,
        journalDb = journalDb,
      )
      val httpService = JournalHttpService(
        clock = clock,
        attachmentStore = attachmentStore,
        journalDb = journalDb,
        publishSiteJobQueue = platform.jobQueueFactory.get(SitePublisher.QueueName),
        publishTracker = publishTracker,
      )
      val jobHandlerFactory = JournalJobHandlerFactory(
        sitePublisher = sitePublisher,
      )
      return JournalWasmoApp(
        journalDb = journalDb,
        publishTracker = publishTracker,
        httpService = httpService,
        jobHandlerFactory = jobHandlerFactory,
      )
    }
  }
}

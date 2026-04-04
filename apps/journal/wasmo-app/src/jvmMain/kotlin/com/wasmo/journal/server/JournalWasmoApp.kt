package com.wasmo.journal.server

import com.wasmo.journal.db.JournalDbService
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

  class Factory : WasmoApp.Factory {
    override suspend fun create(platform: Platform): JournalWasmoApp {
      val clock = platform.clock
      val journalDb = JournalDbService(
        driver = platform.sqlService.getOrCreate().driver(),
      )
      return JournalWasmoApp(
        journalDb = journalDb,
        httpService = JournalHttpService(
          clock = clock,
          objectStore = platform.objectStore,
          journalDb = journalDb,
        ),
        jobHandlerFactory = JournalJobHandlerFactory(),
      )
    }
  }
}

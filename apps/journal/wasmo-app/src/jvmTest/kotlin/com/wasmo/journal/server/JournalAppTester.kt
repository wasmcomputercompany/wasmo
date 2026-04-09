package com.wasmo.journal.server

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.journal.server.publishing.SitePublisher
import wasmo.app.FakePlatform
import wasmo.sql.testSqlService
import wasmo.time.FakeClock

class JournalAppTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val app: JournalWasmoApp
    get() = run!!.app
  val platform: FakePlatform
    get() = run!!.platform
  val httpService: JournalHttpService
    get() = app.httpService
  val sitePublisher: SitePublisher
    get() = app.jobHandlerFactory.sitePublisher
  val clock: FakeClock
    get() = platform.clock

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    testSqlService(
      databaseName = "journal_test",
      clearSchema = true,
    ).use { sqlService ->
      val platform = FakePlatform(
        sqlService = sqlService,
      )
      val app = JournalWasmoApp.Factory(prettyPrint = true)
        .create(platform)

      val run = Run(
        platform = platform,
        app = app,
      )

      run.app.afterInstall(
        oldVersion = 0L,
        newVersion = 1L,
      )

      this.run = run
      try {
        testFunction()
      } finally {
        this.run = null
      }
    }
  }

  private class Run(
    val platform: FakePlatform,
    val app: JournalWasmoApp,
  )
}

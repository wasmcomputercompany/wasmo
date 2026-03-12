package com.wasmo.hello.server

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.sql.jdbc.asSqlService
import com.wasmo.sql.jdbc.connectPostgresql
import com.wasmo.testing.sql.TestDatabaseAddress
import com.wasmo.testing.sql.clearSchema
import wasmo.app.FakePlatform
import wasmo.time.FakeClock

class HelloAppTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val clock: FakeClock
    get() = run!!.platform.clock
  val app: HelloWasmoApp
    get() = run!!.app

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val dataSource = connectPostgresql(TestDatabaseAddress)
    dataSource.clearSchema()

    val platform = FakePlatform(
      sqlService = dataSource.asSqlService(),
    )
    val sqlDatabase = platform.sqlService.getOrCreate()

    val app = HelloWasmoApp(
      platform = platform,
      sqlDatabase = sqlDatabase,
    )

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

  private class Run(
    val platform: FakePlatform,
    val app: HelloWasmoApp,
  )
}

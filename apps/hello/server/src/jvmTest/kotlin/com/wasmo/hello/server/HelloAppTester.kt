package com.wasmo.hello.server

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.sql.r2dbc.asSqlService
import com.wasmo.sql.r2dbc.connectPostgresqlAsync
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
    val dataSource = connectPostgresqlAsync(TestDatabaseAddress)
    val sqlService = dataSource.asSqlService()
    dataSource.clearSchema()

    val platform = FakePlatform(
      sqlService = sqlService,
    )
    val app = HelloWasmoApp.Factory().create(platform)

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

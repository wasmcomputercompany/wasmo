package com.wasmo.sql.vertx

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.testing.sql.TestDatabaseAddress
import com.wasmo.testing.sql.clearSchema
import io.vertx.sqlclient.Pool
import wasmo.sql.SqlService

class VertxPostgresTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val connectionPool: Pool
    get() = run!!.pool
  val sqlService: SqlService
    get() = run!!.sqlService

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val pool = connectVertxPostgresql(TestDatabaseAddress)
    pool.clearSchema()

    run = Run(
      pool = pool,
      sqlService = pool.asSqlService(),
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val pool: Pool,
    val sqlService: SqlService,
  )
}

package com.wasmo.sql.r2dbc

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.testing.sql.TestDatabaseAddress
import com.wasmo.testing.sql.clearSchema
import io.r2dbc.pool.ConnectionPool
import wasmo.sql.SqlService

class R2dbcTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val connectionPool: ConnectionPool
    get() = run!!.connectionPool
  val sqlService: SqlService
    get() = run!!.sqlService

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val connectionPool = connectPostgresqlAsync(TestDatabaseAddress)
    connectionPool.clearSchema()

    run = Run(
      connectionPool = connectionPool,
      sqlService = connectionPool.asSqlService(),
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val connectionPool: ConnectionPool,
    val sqlService: SqlService,
  )
}

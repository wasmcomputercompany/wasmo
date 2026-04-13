package com.wasmo.sql

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.testing.sql.TestDatabaseAddress
import com.wasmo.testing.sql.clearSchema
import wasmo.sql.SqlService

class PostgresqlTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val postgresqlAddress: PostgresqlAddress
    get() = run!!.postgresqlAddress
  val sqlService: SqlService
    get() = run!!.sqlService

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    TestDatabaseAddress.use { connection ->
      connection.clearSchema()
    }

    run = Run(
      postgresqlAddress = TestDatabaseAddress,
      sqlService = TestDatabaseAddress.asSqlService(),
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val postgresqlAddress: PostgresqlAddress,
    val sqlService: SqlService,
  )
}

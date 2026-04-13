package com.wasmo.sql.r2dbc

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.testing.sql.TestDatabaseAddress
import com.wasmo.testing.sql.clearSchema
import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import wasmo.sql.SqlService

class R2dbcTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val postgresql: Postgresql
    get() = run!!.postgresql
  val sqlService: SqlService
    get() = run!!.sqlService

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val postgresql = connectPostgresqlAsync(TestDatabaseAddress)
    postgresql.clearSchema()

    run = Run(
      postgresql = postgresql,
      sqlService = postgresql.asSqlService(),
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val postgresql: Postgresql,
    val sqlService: SqlService,
  )
}

package com.wasmo.sql.testing

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.asSqlService
import wasmo.sql.SqlService

class PostgresqlTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val client: PostgresqlClient
    get() = run!!.client
  val sqlService: SqlService
    get() = run!!.sqlService

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val client = PostgresqlClient(TestDatabaseAddress)
    client.withConnection { connection ->
      connection.clearSchema()
    }

    run = Run(
      client = client,
      sqlService = client.asSqlService(),
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val client: PostgresqlClient,
    val sqlService: SqlService,
  )
}

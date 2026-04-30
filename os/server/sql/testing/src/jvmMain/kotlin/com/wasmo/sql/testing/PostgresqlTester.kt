package com.wasmo.sql.testing

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.asSqlDatabase
import wasmo.sql.SqlDatabase

class PostgresqlTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val client: PostgresqlClient
    get() = run!!.client
  val sqlDatabase: SqlDatabase
    get() = run!!.sqlDatabase

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val postgresqlClientFactory = PostgresqlClient.Factory()
    postgresqlClientFactory.connect(TestDatabaseAddress).use { client ->
      client.withConnection {
        clearSchema()
      }

      run = Run(
        client = client,
        sqlDatabase = client.asSqlDatabase(),
      )
      try {
        testFunction.invoke()
      } finally {
        run = null
      }
    }
  }

  private class Run(
    val client: PostgresqlClient,
    val sqlDatabase: SqlDatabase,
  )
}

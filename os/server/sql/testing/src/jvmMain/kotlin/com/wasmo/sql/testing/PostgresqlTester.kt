package com.wasmo.sql.testing

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.api.SqlEventListener
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.RealOsDatabaseInitializer
import com.wasmo.sql.asSqlDatabase
import wasmo.sql.SqlDatabase

class PostgresqlTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val events: MutableList<SqlEventListener.SqlEvent> = mutableListOf()

  val client: PostgresqlClient
    get() = run!!.client
  val sqlDatabase: SqlDatabase
    get() = run!!.sqlDatabase

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val postgresqlClientFactory = PostgresqlClient.Factory { events.add(it) }
    val prefix = ProjectPrefix.detect()
    val postgresqlConfig = testPostgresqlConfig(prefix)
    val initializer = RealOsDatabaseInitializer(
      clientFactory = postgresqlClientFactory,
      config = postgresqlConfig,
    )
    initializer.initialize()
    initializer.dangerouslyClearSchema()
    postgresqlClientFactory.connect(postgresqlConfig.os).use { client ->
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

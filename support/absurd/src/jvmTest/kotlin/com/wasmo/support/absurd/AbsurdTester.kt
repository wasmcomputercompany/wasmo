package com.wasmo.support.absurd

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode
import kotlin.time.Clock
import kotlinx.coroutines.reactive.awaitLast
import okio.FileSystem
import okio.Path.Companion.toPath

class AbsurdTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val clock: Clock
    get() = run!!.clock
  val postgresql: Postgresql
    get() = run!!.postgresql
  val absurd: Absurd
    get() = run!!.absurd

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val configuration = PostgresqlConnectionConfiguration.builder()
      .host("localhost")
      .username("postgres")
      .password("password")
      .database("absurd_test")
      .sslMode(SSLMode.DISABLE)
      .build()

    val postgresql = Postgresql(PostgresqlConnectionFactory(configuration))
    postgresql.withConnection {
      execute("DROP SCHEMA public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO postgres")
      execute("GRANT ALL ON SCHEMA public TO public")
    }

    postgresql.withConnection {
      val batch = createBatch()
      batch.add(
        FileSystem.RESOURCES.read("/absurd/sql/absurd.sql".toPath()) {
          readUtf8()
        },
      )
      batch.execute().awaitLast()
    }

    run = Run(
      clock = Clock.System,
      postgresql = postgresql,
      absurd = RealAbsurd(postgresql),
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val clock: Clock,
    val postgresql: Postgresql,
    val absurd: Absurd,
  )
}

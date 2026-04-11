package com.wasmo.support.absurd

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import io.r2dbc.postgresql.client.SSLMode
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.reactive.awaitLast
import okio.FileSystem
import okio.Path.Companion.toPath

class AbsurdTester : CoroutineTestInterceptor {
  private var run: Run? = null

  val clock: FakeClock
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

    val postgresql = Postgresql(configuration)
    postgresql.withConnection {
      executeVoid("DROP SCHEMA public CASCADE")
      executeVoid("CREATE SCHEMA public")
      executeVoid("GRANT ALL ON SCHEMA public TO postgres")
      executeVoid("GRANT ALL ON SCHEMA public TO public")

      executeVoid("DROP SCHEMA absurd CASCADE")
      executeVoid("CREATE SCHEMA absurd")
      executeVoid("GRANT ALL ON SCHEMA absurd TO postgres")
      executeVoid("GRANT ALL ON SCHEMA absurd TO public")
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

    val clock = FakeClock(postgresql)
    clock.flushToPostgresql()

    val absurd = Absurd(clock, postgresql)
    absurd.createQueue()

    run = Run(
      clock = clock,
      postgresql = postgresql,
      absurd = absurd,
    )
    try {
      testFunction.invoke()
    } finally {
      run = null
    }
  }

  private class Run(
    val clock: FakeClock,
    val postgresql: Postgresql,
    val absurd: Absurd,
  )

  class FakeClock(
    private val postgresql: Postgresql,
  ) : Clock {
    private var now = Instant.parse("2025-10-20T00:00:00Z")

    override fun now() = now

    suspend fun sleep(duration: Duration) {
      now += duration
      flushToPostgresql()
    }

    /** `absurd.sql` supports faking out the clock for testing. */
    suspend fun flushToPostgresql() {
      postgresql.withConnection {
        val rowCount = execute(
          "SELECT set_config($1, $2, $3)",
          "absurd.fake_now",
          now.toString(),
          false,
        )
        println(rowCount)
      }
    }
  }
}

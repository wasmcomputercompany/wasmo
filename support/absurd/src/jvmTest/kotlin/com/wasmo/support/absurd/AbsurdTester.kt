package com.wasmo.support.absurd

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import assertk.assertThat
import assertk.assertions.containsExactly
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import io.r2dbc.postgresql.client.SSLMode
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.reactive.awaitLast
import okio.FileSystem
import okio.Path.Companion.toPath

class AbsurdTester : CoroutineTestInterceptor, Log {
  private var run: Run? = null

  val clock: FakeClock
    get() = run!!.clock
  val postgresql: Postgresql
    get() = run!!.postgresql
  private val log: Channel<String>
    get() = run!!.log

  fun sandwichMaker() = SandwichMaker(this)

  suspend fun absurd(vararg registrations: TaskRegistration<*, *>): Absurd {
    val result = Absurd(
      clock = clock,
      postgresql = postgresql,
      registrations = registrations.toList(),
    )
    result.createQueue()
    return result
  }

  override fun log(message: String) {
    check(log.trySend(message).isSuccess)
  }

  fun assertLogs(vararg messages: String) {
    assertThat(log.receiveAvailable()).containsExactly(*messages)
  }

  private fun <T> Channel<T>.receiveAvailable(): List<T> {
    return buildList {
      while (true) {
        val receive = tryReceive()
        if (!receive.isSuccess) break
        add(receive.getOrThrow())
      }
    }
  }


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

    val log = Channel<String>(capacity = Int.MAX_VALUE)

    run = Run(
      clock = clock,
      postgresql = postgresql,
      log = log,
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
    val log: Channel<String>,
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

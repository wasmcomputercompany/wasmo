package com.wasmo.support.absurd

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import assertk.assertThat
import assertk.assertions.containsExactly
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.channels.Channel
import okio.FileSystem
import okio.Path.Companion.toPath

class AbsurdTester : CoroutineTestInterceptor, Log {
  private var run: Run? = null

  val clock: FakeClock
    get() = run!!.clock
  val postgresql: PostgresqlClient
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
    val connectOptions = PgConnectOptions()
      .setHost("localhost")
      .setDatabase("absurd_test")
      .setUser("postgres")
      .setPassword("password")
      .setSslMode(SslMode.DISABLE)

    val postgresql = PostgresqlClient(connectOptions)
    postgresql.withConnection<Unit> {
      execute("DROP SCHEMA public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO postgres")
      execute("GRANT ALL ON SCHEMA public TO public")

      execute("DROP SCHEMA absurd CASCADE")
      execute("CREATE SCHEMA absurd")
      execute("GRANT ALL ON SCHEMA absurd TO postgres")
      execute("GRANT ALL ON SCHEMA absurd TO public")

      execute(
        FileSystem.RESOURCES.read("/absurd/sql/absurd.sql".toPath()) {
          readUtf8()
        }
      )
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
    val postgresql: PostgresqlClient,
    val log: Channel<String>,
  )

  class FakeClock(
    private val postgresql: PostgresqlClient,
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
        execute(
          "SELECT set_config($1, $2, $3)",
          "absurd.fake_now",
          now.toString(),
          false,
        )
      }
    }
  }
}

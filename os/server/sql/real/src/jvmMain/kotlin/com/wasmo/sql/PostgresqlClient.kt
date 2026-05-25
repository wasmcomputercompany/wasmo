package com.wasmo.sql

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import okio.Closeable

/**
 * Manages connections to the Postgresql server.
 */
class PostgresqlClient(
  private val address: PostgresqlAddress,
  private val pool: Pool,
) : Closeable {
  suspend fun <T> withConnection(block: suspend SqlClient.() -> T): T {
    val connection = connect()
    try {
      return block(connection)
    } finally {
      connection.close().awaitSuspending()
    }
  }

  suspend fun connect(): SqlClient =
    pool.connection.awaitSuspending(
      onCancellation = { _, value, _ -> value.close() },
    )

  override fun close() {
    pool.close()
  }

  override fun toString() = "PostgresqlClient($address)"

  @Inject
  @SingleIn(OsScope::class)
  class Factory {
    fun connect(
      address: PostgresqlAddress,
    ): PostgresqlClient {
      val connectOptions = PgConnectOptions().apply {
        host = address.hostname
        port = address.port
        database = address.databaseName
        user = address.user
        password = address.password
        sslMode = when {
          address.ssl -> SslMode.VERIFY_FULL
          else -> SslMode.DISABLE
        }
        reconnectAttempts = 5
        reconnectInterval = 500.milliseconds.inWholeMilliseconds
        addProperty("application_name", "wasmo")
      }

      val pool = PgBuilder.pool()
        .apply {
          connectingTo(connectOptions)
          with(
            PoolOptions()
              .apply {
                maxSize = 64
                maxWaitQueueSize = 64
                idleTimeout = 30.seconds.inWholeMilliseconds.toInt()
                connectionTimeout = 1.seconds.inWholeMilliseconds.toInt()
                maxLifetime = 60.seconds.inWholeMilliseconds.toInt()
              },
          )
        }
        .build()

      return PostgresqlClient(address, pool)
    }
  }
}

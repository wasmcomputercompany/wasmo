package com.wasmo.sql

import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.SqlClient
import okio.Closeable

/**
 * Manages connections to the Postgresql server.
 *
 * TODO: impose a limit on how many connections may be open at once, and make callers wait for a
 *   connection when necessary.
 */
class PostgresqlClient(
  private val pool: Pool,
) : Closeable {
  suspend fun <T> withConnection(block: suspend SqlClient.() -> T): T {
    val connection = connect()
    try {
      return block(connection)
    } finally {
      connection.close().asDeferred().await()
    }
  }

  suspend fun connect(): SqlClient =
    pool.connection.asDeferred().await()

  override fun close() {
    pool.close()
  }

  class Factory {
    fun connect(
      address: PostgresqlAddress,
    ): PostgresqlClient {
      val connectOptions = PgConnectOptions()
        .setHost(address.hostname)
        .setDatabase(address.databaseName)
        .setUser(address.user)
        .setPassword(address.password)
        .setSslMode(
          when {
            address.ssl -> SslMode.VERIFY_FULL
            else -> SslMode.DISABLE
          },
        )

      val pool = PgBuilder
        .pool()
        .connectingTo(connectOptions)
        .build()

      return PostgresqlClient(pool)
    }
  }
}

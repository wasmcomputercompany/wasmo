package com.wasmo.sql

import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import io.vertx.sqlclient.SqlClient

/**
 * Manages connections to the Postgresql server.
 *
 * TODO: impose a limit on how many connections may be open at once, and make callers wait for a
 *   connection when necessary.
 */
class PostgresqlClient(
  address: PostgresqlAddress,
) {
  private val connectOptions = PgConnectOptions()
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

  /** Returns a new connection that the caller must close when they're done with it. */
  suspend fun connect(): SqlClient {
    return PgBuilder
      .client()
      .connectingTo(connectOptions)
      .build()
  }

  suspend fun <T> withConnection(block: suspend (SqlClient) -> T): T {
    val connection = connect()
    try {
      return block(connection)
    } finally {
      connection.close().asDeferred().await()
    }
  }
}

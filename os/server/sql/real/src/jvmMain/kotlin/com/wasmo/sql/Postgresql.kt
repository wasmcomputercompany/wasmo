package com.wasmo.sql

import io.vertx.core.Future
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred

/** Returns a new connection that the caller must close when they're done with it. */
fun PostgresqlAddress.connect(): SqlClient {
  val connectOptions = PgConnectOptions()
    .setHost(hostname)
    .setDatabase(databaseName)
    .setUser(user)
    .setPassword(password)
    .setSslMode(
      when {
        ssl -> SslMode.VERIFY_FULL
        else -> SslMode.DISABLE
      },
    )

  return PgBuilder
    .client()
    .connectingTo(connectOptions)
    .build()
}

suspend fun <T> PostgresqlAddress.use(block: suspend (SqlClient) -> T): T {
  val connection = connect()
  try {
    return block(connection)
  } finally {
    connection.close().asDeferred().await()
  }
}

suspend inline fun SqlClient.execute(
  sql: String,
): RowSet<Row> {
  return query(sql).execute().asDeferred().await()
}

fun <T> Future<T>.asDeferred(): Deferred<T> =
  toCompletionStage().asDeferred()

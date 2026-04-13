package com.wasmo.sql.vertx

import com.wasmo.sql.PostgresqlAddress
import io.vertx.core.Future
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred

fun connectVertxPostgresql(
  address: PostgresqlAddress,
  poolSize: Int = 1,
): Pool {
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

  val poolOptions = PoolOptions()
    .setMaxSize(poolSize)

  return PgBuilder
    .pool()
    .with(poolOptions)
    .connectingTo(connectOptions)
    .build()
}

suspend inline fun SqlConnection.execute(
  sql: String,
): RowSet<Row> {
  return query(sql).execute().asDeferred().await()
}

suspend inline fun <T> Pool.useConnection(
  block: suspend SqlConnection.() -> T,
): T {
  val connection = this.connection.asDeferred().await()
  try {
    return connection.block()
  } finally {
    connection.close().asDeferred().await()
  }
}

fun <T> Future<T>.asDeferred(): Deferred<T> =
  toCompletionStage().asDeferred()

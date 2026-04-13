package com.wasmo.sql

import io.vertx.core.Future
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred

suspend inline fun SqlClient.execute(
  sql: String,
): RowSet<Row> {
  return query(sql).execute().asDeferred().await()
}

fun <T> Future<T>.asDeferred(): Deferred<T> =
  toCompletionStage().asDeferred()

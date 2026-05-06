package com.wasmo.sql

import io.vertx.core.Completable
import io.vertx.core.Future
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend inline fun SqlClient.execute(
  sql: String,
): RowSet<Row> {
  return query(sql).execute().awaitSuspending()
}

/** @param onCancellation to release the result value if it holds any resources. */
suspend fun <T> Future<T>.awaitSuspending(
  onCancellation: ((cause: Throwable, value: T, context: CoroutineContext) -> Unit)? = null,
): T {
  val future = this
  return suspendCancellableCoroutine { continuation ->
    future.onComplete(
      Completable<T> { result, failure ->
        when {
          failure == null -> continuation.resume(result, onCancellation)
          else -> continuation.resumeWithException(failure)
        }
      },
    )
  }
}

@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.vertx.core.Completable
import io.vertx.core.Future
import io.vertx.core.json.Json.CODEC
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import io.vertx.sqlclient.data.NullValue
import java.time.ZoneOffset
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import okio.Closeable

/*
 * This file attempts to make Postgresql a little bit friendlier to interact with from
 * Kotlin + Coroutines.
 */

class PostgresqlClient(
  connectOptions: PgConnectOptions,
) : Closeable {
  private val pool = PgBuilder
    .pool()
    .connectingTo(connectOptions)
    .build()

  /** Returns a new connection that the caller must close when they're done with it. */
  suspend fun connect(): SqlClient = pool.connection.awaitSuspending(
    onCancellation = { _, value, _ -> value.close() },
  )

  suspend inline fun <T> withConnection(block: suspend SqlClient.() -> T): T {
    val connection = connect()
    try {
      return block(connection)
    } finally {
      connection.close().awaitSuspending()
    }
  }

  override fun close() {
    pool.close()
  }
}

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

internal fun Tuple.addJson(value: JsonElement?): Tuple {
  return addValue(
    when (value) {
      null -> NullValue.JsonObject
      else -> CODEC.fromString(KotlinJson.encodeToString(value), Any::class.java)
    },
  )
}

internal fun Tuple.addUuid(value: Uuid): Tuple {
  return addUUID((value as Uuid?)?.toJavaUuid())
}

internal fun Tuple.addInstant(value: Instant?): Tuple {
  return addOffsetDateTime(value?.toJavaInstant()?.atOffset(ZoneOffset.UTC))
}

internal suspend inline fun SqlClient.execute(
  sql: String,
  tuple: Tuple,
): Long {
  val result = preparedQuery(sql).execute(tuple).awaitSuspending()
  return result.rowCount().toLong()
}

internal suspend inline fun <R : Any> SqlClient.executeQuery(
  sql: String,
  tuple: Tuple,
  noinline rowMapper: Row.() -> R,
): List<R> {
  val result = preparedQuery(sql).execute(tuple).awaitSuspending()
  return result.map(rowMapper)
}

internal suspend fun SqlClient.begin() {
  execute("BEGIN")
}

internal suspend fun SqlClient.rollback() {
  execute("ROLLBACK")
}

@PublishedApi
internal val KotlinJson = Json {
  ignoreUnknownKeys = true
}

internal fun Row.getUuid(name: String): Uuid = getUUID(name)!!.toKotlinUuid()

internal fun Row.getJsonElement(name: String): JsonElement? {
  val jsonModel = getJson(name) ?: return null
  val jsonString = CODEC.toString(jsonModel)
  return KotlinJson.parseToJsonElement(jsonString)
}

internal fun <T : Any> Row.decodeJson(name: String, serializer: KSerializer<T>): T? {
  val jsonModel = getJson(name) ?: return null
  val jsonString = CODEC.toString(jsonModel)
  return KotlinJson.decodeFromString(serializer, jsonString)
}

internal inline fun <reified T : Any> Row.decodeJsonOrNull(name: String): T? =
  decodeJson(name, serializer<T>())

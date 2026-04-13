@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

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
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/*
 * This file attempts to make Postgresql a little bit friendlier to interact with from
 * Kotlin + Coroutines.
 */

class PostgresqlClient(
  private val connectOptions: PgConnectOptions,
) {
  /** Returns a new connection that the caller must close when they're done with it. */
  suspend fun connect(): SqlClient {
    return PgBuilder
      .client()
      .connectingTo(connectOptions)
      .build()
  }

  suspend inline fun <T> withConnection(block: suspend SqlClient.() -> T): T {
    val connection = connect()
    try {
      return block(connection)
    } finally {
      connection.close().asDeferred().await()
    }
  }
}

suspend inline fun SqlClient.execute(
  sql: String,
): RowSet<Row> {
  return query(sql).execute().asDeferred().await()
}

fun <T> Future<T>.asDeferred(): Deferred<T> =
  toCompletionStage().asDeferred()

internal inline fun <reified T> Tuple.add(value: T?) {
  when {
    T::class == Boolean::class -> addBoolean(value as Boolean?)
    T::class == String::class -> addString(value as String?)
    T::class == Int::class -> addInteger(value as Int?)
    T::class == Uuid::class -> addUUID((value as Uuid?)?.toJavaUuid())
    T::class == kotlin.time.Instant::class -> addOffsetDateTime((value as kotlin.time.Instant?)?.toJavaInstant()?.atOffset(ZoneOffset.UTC))
    T::class == JsonElement::class -> {
      if (value == null) {
        addValue(NullValue.JsonObject)
      } else {
        addValue(
          CODEC.fromString(KotlinJson.encodeToString(value as JsonElement), Any::class.java),
        )
      }
    }
    else -> error("unexpected type: ${T::class}")
  }
}

internal suspend inline fun <reified P0> SqlClient.execute(
  sql: String,
  p0: P0,
): Long {
  val tuple = Tuple.tuple().apply {
    add(p0)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.rowCount().toLong()
}

internal suspend inline fun <reified P0, reified P1> SqlClient.execute(
  sql: String,
  p0: P0,
  p1: P1,
): Long {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.rowCount().toLong()
}

internal suspend inline fun <reified P0, reified P1, reified P2> SqlClient.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
): Long {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.rowCount().toLong()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3> SqlClient.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
): Long {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
    add(p3)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.rowCount().toLong()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> SqlClient.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
): Long {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
    add(p3)
    add(p4)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.rowCount().toLong()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, reified P5> SqlClient.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  p5: P5,
): Long {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
    add(p3)
    add(p4)
    add(p5)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.rowCount().toLong()
}

internal suspend inline fun <reified P0, reified P1, R : Any> SqlClient.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  noinline rowMapper: Row.() -> R,
): List<R> {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.map(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, R : Any> SqlClient.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  noinline rowMapper: Row.() -> R,
): List<R> {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.map(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, R : Any> SqlClient.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  noinline rowMapper: Row.() -> R,
): List<R> {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
    add(p3)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.map(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, R : Any> SqlClient.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  noinline rowMapper: Row.() -> R,
): List<R> {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
    add(p3)
    add(p4)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.map(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, reified P5, R : Any> SqlClient.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  p5: P5,
  noinline rowMapper: Row.() -> R,
): List<R> {
  val tuple = Tuple.tuple().apply {
    add(p0)
    add(p1)
    add(p2)
    add(p3)
    add(p4)
    add(p5)
  }
  val result = preparedQuery(sql).execute(tuple).asDeferred().await()
  return result.map(rowMapper)
}

@PublishedApi
internal val KotlinJson = Json {
  ignoreUnknownKeys = true
}

internal fun Row.uuid(name: String): Uuid = getUUID(name)!!.toKotlinUuid()

internal fun Row.boolean(name: String): Boolean = getBoolean(name)!!

internal fun Row.int(name: String): Int = getInteger(name)!!

internal fun Row.stringOrNull(name: String): String? = getString(name)

internal fun Row.string(name: String): String = stringOrNull(name)!!

internal fun Row.rawJson(name: String): JsonElement = rawJsonOrNull(name)!!

internal fun Row.rawJsonOrNull(name: String): JsonElement? {
  val jsonModel = getJson(name) ?: return null
  val jsonString = CODEC.toString(jsonModel)
  return KotlinJson.parseToJsonElement(jsonString)
}

internal fun <T : Any> Row.jsonOrNull(name: String, serializer: KSerializer<T>): T? {
  val jsonModel = getJson(name) ?: return null
  val jsonString = CODEC.toString(jsonModel)
  return KotlinJson.decodeFromString(serializer, jsonString)
}

internal inline fun <reified T : Any> Row.jsonOrNull(name: String): T? =
  jsonOrNull(name, serializer<T>())

internal fun <T : Any> Row.json(name: String, serializer: KSerializer<T>): T =
  jsonOrNull(name, serializer)!!

internal inline fun <reified T : Any> Row.json(name: String): T =
  jsonOrNull(name, serializer<T>())!!

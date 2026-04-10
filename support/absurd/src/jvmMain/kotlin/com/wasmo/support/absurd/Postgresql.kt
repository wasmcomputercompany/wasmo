@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import io.r2dbc.postgresql.api.PostgresqlConnection as Connection
import io.r2dbc.postgresql.api.PostgresqlResult
import io.r2dbc.postgresql.api.PostgresqlStatement
import io.r2dbc.postgresql.codec.Json as PostgresqlJson
import io.r2dbc.spi.Readable
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/*
 * This file attempts to make R2DBC + Postgresql a little bit friendlier to interact with from
 * Kotlin + Coroutines.
 */

internal suspend inline fun <T> Postgresql.withConnection(
  block: suspend Connection.() -> T,
): T {
  val connection = create().awaitSingle()
  try {
    return connection.block()
  } finally {
    connection.close().subscribe()
  }
}

inline fun <reified T> PostgresqlStatement.bindNullable(index: Int, value: T?) {
  when {
    value != null -> bind(index, value)
    else -> bindNull(index, T::class.java)
  }
}

internal suspend fun PostgresqlStatement.rowCount(): Long =
  execute().awaitSingle().rowsUpdated.awaitSingle()

internal suspend fun <R : Any> PostgresqlStatement.rows(
  rowMapper: Readable.() -> R,
): List<R> = execute().awaitSingle().map(rowMapper).asFlow().toList()

internal suspend inline fun Connection.executeVoid(
  sql: String,
): PostgresqlResult = createStatement(sql).run {
  execute().awaitSingle()
}

internal suspend inline fun Connection.execute(
  sql: String,
): Long = createStatement(sql).rowCount()

internal suspend inline fun <reified P0> Connection.execute(
  sql: String,
  p0: P0,
): Long = createStatement(sql).run {
  bindNullable(0, p0 ?: P0::class.java)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  rowCount()
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, reified P5> Connection.execute(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  p5: P5,
): Long = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  bindNullable(5, p5)
  rowCount()
}

internal suspend inline fun <R : Any> Connection.executeQuery(
  sql: String,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).rows(rowMapper)

internal suspend inline fun <reified P0, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0 ?: P0::class.java)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  rows(rowMapper)
}

internal suspend inline fun <reified P0, reified P1, reified P2, reified P3, reified P4, R : Any> Connection.executeQuery(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
  noinline rowMapper: Readable.() -> R,
): List<R> = createStatement(sql).run {
  bindNullable(0, p0)
  bindNullable(1, p1)
  bindNullable(2, p2)
  bindNullable(3, p3)
  bindNullable(4, p4)
  rows(rowMapper)
}

@PublishedApi
internal val KotlinJson = Json {
  ignoreUnknownKeys = true
}

internal fun Readable.uuid(name: String): Uuid = get(name, UUID::class.java)!!.toKotlinUuid()

internal fun Readable.int(name: String): Int = get(name, Int::class.java)!!

internal fun Readable.stringOrNull(name: String): String? = get(name, String::class.java)

internal fun Readable.string(name: String): String = stringOrNull(name)!!

internal fun Readable.rawJson(name: String): PostgresqlJson =
  get(name, PostgresqlJson::class.java)!!

internal fun <T : Any> Readable.jsonOrNull(name: String, serializer: KSerializer<T>): T? {
  val json = get(name, PostgresqlJson::class.java) ?: return null
  return KotlinJson.decodeFromString(serializer, json.asString())
}

internal inline fun <reified T : Any> Readable.jsonOrNull(name: String): T? =
  jsonOrNull(name, serializer<T>())

internal fun <T : Any> Readable.json(name: String, serializer: KSerializer<T>): T =
  jsonOrNull(name, serializer)!!

internal inline fun <reified T : Any> Readable.json(name: String): T =
  jsonOrNull(name, serializer<T>())!!


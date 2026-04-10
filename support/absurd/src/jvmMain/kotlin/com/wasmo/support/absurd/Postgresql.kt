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
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.KSerializer
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

internal suspend fun Connection.execute(
  sql: String,
): PostgresqlResult {
  return createStatement(sql).execute().awaitSingle()
}

internal inline fun <reified P0> Connection.createStatement(
  sql: String,
  p0: P0,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
}

internal inline fun <reified P0, reified P1> Connection.createStatement(
  sql: String,
  p0: P0,
  p1: P1,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
  bind(1, p1 ?: P1::class.java)
}

internal inline fun <reified P0, reified P1, reified P2> Connection.createStatement(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
  bind(1, p1 ?: P1::class.java)
  bind(2, p2 ?: P2::class.java)
}

internal inline fun <reified P0, reified P1, reified P2, reified P3> Connection.createStatement(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
  bind(1, p1 ?: P1::class.java)
  bind(2, p2 ?: P2::class.java)
  bind(3, p3 ?: P3::class.java)
}

internal inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> Connection.createStatement(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
  p3: P3,
  p4: P4,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
  bind(1, p1 ?: P1::class.java)
  bind(2, p2 ?: P2::class.java)
  bind(3, p3 ?: P3::class.java)
  bind(4, p4 ?: P4::class.java)
}

@PublishedApi
internal val KotlinJson = kotlinx.serialization.json.Json {
  ignoreUnknownKeys = true
}

internal fun Readable.uuid(name: String): Uuid = get(name, UUID::class.java)!!.toKotlinUuid()

internal fun Readable.int(name: String): Int = get(name, Int::class.java)!!

internal fun Readable.stringOrNull(name: String): String? = get(name, String::class.java)

internal fun Readable.string(name: String): String = stringOrNull(name)!!

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


@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
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

class Postgresql(
  @PublishedApi
  internal val postgresqlConnectionFactory: PostgresqlConnectionFactory,
) {
  suspend inline fun <T> withConnection(block: suspend PostgresqlConnection.() -> T): T {
    with(postgresqlConnectionFactory.create().awaitSingle()) {
      try {
        return block()
      } finally {
        close().subscribe()
      }
    }
  }
}

suspend fun PostgresqlConnection.execute(
  sql: String,
): PostgresqlResult {
  return createStatement(sql).execute().awaitSingle()
}

inline fun <reified P0> PostgresqlConnection.createStatement(
  sql: String,
  p0: P0,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
}

inline fun <reified P0, reified P1> PostgresqlConnection.createStatement(
  sql: String,
  p0: P0,
  p1: P1,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
  bind(1, p1 ?: P1::class.java)
}

inline fun <reified P0, reified P1, reified P2> PostgresqlConnection.createStatement(
  sql: String,
  p0: P0,
  p1: P1,
  p2: P2,
): PostgresqlStatement = createStatement(sql).apply {
  bind(0, p0 ?: P0::class.java)
  bind(1, p1 ?: P1::class.java)
  bind(2, p2 ?: P2::class.java)
}

inline fun <reified P0, reified P1, reified P2, reified P3> PostgresqlConnection.createStatement(
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

inline fun <reified P0, reified P1, reified P2, reified P3, reified P4> PostgresqlConnection.createStatement(
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

fun Readable.uuid(name: String): Uuid = get(name, UUID::class.java)!!.toKotlinUuid()

fun Readable.int(name: String): Int = get(name, Int::class.java)!!

fun Readable.stringOrNull(name: String): String? = get(name, String::class.java)

fun Readable.string(name: String): String = stringOrNull(name)!!

fun <T : Any> Readable.jsonOrNull(name: String, serializer: KSerializer<T>): T? {
  val json = get(name, PostgresqlJson::class.java) ?: return null
  return KotlinJson.decodeFromString(serializer, json.asString())
}

inline fun <reified T : Any> Readable.jsonOrNull(name: String): T? =
  jsonOrNull(name, serializer<T>())

fun <T : Any> Readable.json(name: String, serializer: KSerializer<T>): T =
  jsonOrNull(name, serializer)!!

inline fun <reified T : Any> Readable.json(name: String): T = jsonOrNull(name, serializer<T>())!!


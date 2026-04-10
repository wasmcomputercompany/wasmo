package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.postgresql.api.PostgresqlResult
import io.r2dbc.postgresql.api.PostgresqlStatement
import kotlinx.coroutines.reactive.awaitSingle

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

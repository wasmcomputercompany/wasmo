package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.postgresql.api.PostgresqlResult
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
  vararg params: Any?,
): PostgresqlResult {
  return createStatement(sql).execute().awaitSingle()
}

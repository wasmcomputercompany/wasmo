package com.wasmo.sql.r2dbc

import com.wasmo.sql.PostgresqlAddress
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import io.r2dbc.postgresql.api.PostgresqlConnection as Connection
import io.r2dbc.postgresql.api.PostgresqlResult
import io.r2dbc.postgresql.client.SSLMode
import kotlinx.coroutines.reactive.awaitSingle

fun connectPostgresqlAsync(
  address: PostgresqlAddress,
): PostgresqlConnectionFactory {
  val configuration = PostgresqlConnectionConfiguration.builder()
    .host(address.hostname)
    .username(address.user)
    .password(address.password)
    .database(address.databaseName)
    .sslMode(
      when {
        address.ssl -> SSLMode.VERIFY_FULL
        else -> SSLMode.DISABLE
      },
    )
    .build()
  return PostgresqlConnectionFactory(configuration)
}

suspend inline fun <T> Postgresql.withConnection(
  block: suspend Connection.() -> T,
): T {
  val connection = create().awaitSingle()
  try {
    return connection.block()
  } finally {
    connection.close().subscribe()
  }
}

suspend inline fun Connection.executeVoid(
  sql: String,
): PostgresqlResult = createStatement(sql).run {
  execute().awaitSingle()
}

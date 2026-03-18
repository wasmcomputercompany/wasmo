package com.wasmo.sql.r2dbc

import com.wasmo.sql.PostgresqlAddress
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode

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

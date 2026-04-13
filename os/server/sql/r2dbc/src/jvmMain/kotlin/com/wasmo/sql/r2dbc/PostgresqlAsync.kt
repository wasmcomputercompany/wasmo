package com.wasmo.sql.r2dbc

import com.wasmo.sql.PostgresqlAddress
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import java.time.ZoneOffset
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.reactive.awaitSingle

fun connectPostgresqlAsync(
  address: PostgresqlAddress,
  maxAcquireTime: Duration = 300.milliseconds,
  minSize: Int = 1,
  maxSize: Int = 1,
): ConnectionPool {
  val postgresqlConfiguration = PostgresqlConnectionConfiguration.builder()
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
    .timeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
    .build()

  val poolConfiguration = ConnectionPoolConfiguration.builder()
    .connectionFactory(PostgresqlConnectionFactory(postgresqlConfiguration))
    .maxAcquireTime(maxAcquireTime.toJavaDuration())
    .initialSize(minSize)
    .maxSize(maxSize)
    .build()

  return ConnectionPool(poolConfiguration)
}

suspend inline fun <T> ConnectionFactory.withConnection(
  block: suspend Connection.() -> T,
): T {
  val connection = create().awaitSingle()
  try {
    return connection.block()
  } finally {
    connection.close().subscribeAndDiscard()
  }
}

suspend inline fun Connection.executeVoid(
  sql: String,
): Result {
  return createStatement(sql).run {
    execute().awaitSingle()
  }
}

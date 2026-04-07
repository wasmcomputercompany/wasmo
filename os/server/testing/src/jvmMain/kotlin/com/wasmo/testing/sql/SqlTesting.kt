package com.wasmo.testing.sql

import com.wasmo.sql.PostgresqlAddress
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.commons.dbcp2.PoolableConnection
import org.apache.commons.dbcp2.PoolingDataSource

val TestDatabaseAddress = PostgresqlAddress(
  databaseName = "wasmo_test",
  user = "postgres",
  password = "password",
  hostname = "localhost",
  ssl = false,
)

fun PoolingDataSource<PoolableConnection>.clearSchema() {
  connection.use { connection ->
    connection.prepareStatement("DROP SCHEMA public CASCADE").executeUpdate()
    connection.prepareStatement("CREATE SCHEMA public").executeUpdate()
    connection.prepareStatement("GRANT ALL ON SCHEMA public TO postgres").executeUpdate()
    connection.prepareStatement("GRANT ALL ON SCHEMA public TO public").executeUpdate()
  }
}

suspend fun PostgresqlConnectionFactory.clearSchema() {
  with(create().awaitSingle()) {
    createStatement("DROP SCHEMA public CASCADE").execute().awaitSingle()
    createStatement("CREATE SCHEMA public").execute().awaitSingle()
    createStatement("GRANT ALL ON SCHEMA public TO postgres").execute().awaitSingle()
    createStatement("GRANT ALL ON SCHEMA public TO public").execute().awaitSingle()
    close().subscribe()
  }
}

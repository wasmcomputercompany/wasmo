package com.wasmo.testing.sql

import com.wasmo.sql.jdbc.PostgresqlAddress
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

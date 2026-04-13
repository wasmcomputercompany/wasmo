package com.wasmo.testing.sql

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.r2dbc.executeVoid
import com.wasmo.sql.r2dbc.withConnection
import com.wasmo.sql.vertx.execute
import com.wasmo.sql.vertx.useConnection
import io.r2dbc.spi.ConnectionFactory
import io.vertx.sqlclient.Pool
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
    connection.prepareStatement("DROP SCHEMA IF EXISTS public CASCADE").executeUpdate()
    connection.prepareStatement("CREATE SCHEMA public").executeUpdate()
    connection.prepareStatement("GRANT ALL ON SCHEMA public TO postgres").executeUpdate()
    connection.prepareStatement("GRANT ALL ON SCHEMA public TO public").executeUpdate()
  }
}

suspend fun ConnectionFactory.clearSchema() {
  withConnection {
    executeVoid("DROP SCHEMA IF EXISTS public CASCADE")
    executeVoid("CREATE SCHEMA public")
    executeVoid("GRANT ALL ON SCHEMA public TO postgres")
    executeVoid("GRANT ALL ON SCHEMA public TO public")
  }
}

suspend fun Pool.clearSchema() {
  useConnection {
    execute("DROP SCHEMA IF EXISTS public CASCADE")
    execute("CREATE SCHEMA public")
    execute("GRANT ALL ON SCHEMA public TO postgres")
    execute("GRANT ALL ON SCHEMA public TO public")
  }
}

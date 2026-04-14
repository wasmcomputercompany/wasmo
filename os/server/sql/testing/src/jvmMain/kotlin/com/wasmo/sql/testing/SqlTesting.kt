package com.wasmo.sql.testing

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.execute
import io.vertx.sqlclient.SqlClient

val TestDatabaseAddress = PostgresqlAddress(
  databaseName = "wasmo_test",
  user = "postgres",
  password = "password",
  hostname = "localhost",
  ssl = false,
)

suspend fun SqlClient.clearSchema() {
  execute("DROP SCHEMA IF EXISTS public CASCADE")
  execute("CREATE SCHEMA public")
  execute("GRANT ALL ON SCHEMA public TO postgres")
  execute("GRANT ALL ON SCHEMA public TO public")
}

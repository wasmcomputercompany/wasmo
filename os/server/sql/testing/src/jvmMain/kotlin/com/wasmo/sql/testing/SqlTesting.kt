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

suspend fun SqlClient.dropAppDatabases() {
  val appDatabases = execute(
    """
      SELECT datname
      FROM pg_database
      WHERE datname like 'app_%'
      """)
  appDatabases.forEach {
    val appDatabase = it.getString(0)
    // TODO: Figure out why we need "WITH (FORCE)". We shouldn't if it is cleaning up correctly.
    // Although maybe we leave it anyway.
    execute("DROP DATABASE IF EXISTS $appDatabase WITH (FORCE)")
  }
}

suspend fun SqlClient.dropAppRoles() {
  val appRoles = execute(
    """
      SELECT rolname
      FROM pg_roles
      WHERE rolname like 'app_%'
      """)
  appRoles.forEach {
    val appRole = it.getString(0)
    execute("DROP ROLE IF EXISTS $appRole")
  }
}

package com.wasmo.sql.testing

import com.wasmo.sql.WasmoPostgresqlConfig
import com.wasmo.sql.execute
import io.vertx.sqlclient.SqlClient

val POSTGRESQL_HOSTNAME = System.getenv("POSTGRESQL_HOSTNAME")
  ?: "localhost"

val TestPostgresqlConfig = WasmoPostgresqlConfig(
  hostname = POSTGRESQL_HOSTNAME,
  ssl = false,
  adminUser = "postgres",
  adminPassword = "password",
  adminDatabaseName = "postgres",
  osUser = "wasmo_test",
  osPassword = "password",
  osDatabaseName = "wasmo_test",
)

suspend fun SqlClient.dropAppDatabases() {
  val appDatabases = execute(
    """
    SELECT datname
    FROM pg_database
    WHERE datname like 'ft_app_%'
    """,
  )
  for (row in appDatabases) {
    val appDatabase = row.getString(0)
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
    WHERE rolname like 'ft_app_%'
    """,
  )
  for (row in appRoles) {
    val appRole = row.getString(0)
    execute("DROP ROLE IF EXISTS $appRole")
  }
}

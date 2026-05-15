package com.wasmo.sql.testing

import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.execute
import io.vertx.pgclient.PgException
import io.vertx.sqlclient.SqlClient

val POSTGRESQL_HOSTNAME = System.getenv("POSTGRESQL_HOSTNAME")
  ?: "localhost"

val AdminPostgresqlAddress = PostgresqlAddress(
  databaseName = "postgres",
  user = "postgres",
  password = "password",
  hostname = POSTGRESQL_HOSTNAME,
  ssl = false,
)

val TestPostgresqlAddress = AdminPostgresqlAddress.copy(
  databaseName = "wasmo_test",
  user = "wasmo_test",
)

/** Creates the test database (if it doesn't exist), and clears it. */
suspend fun PostgresqlClient.Factory.prepareTestDatabase(
  superuser: PostgresqlAddress,
  test: PostgresqlAddress,
) {
  // Create the test database if it doesn't exist already. This authenticates as superuser.
  connect(superuser).use { postgresqlClient ->
    try {
      postgresqlClient.withConnection {
        execute("CREATE DATABASE ${test.databaseName} WITH ENCODING = 'UTF8'")
        execute("CREATE USER ${test.user} WITH PASSWORD '${test.password}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
        execute("GRANT ALL PRIVILEGES ON DATABASE ${test.databaseName} TO ${test.user}")
      }
    } catch (_: PgException) {
      // Assume this database exists.
    }
  }

  // Drop the database. This authenticates as superuser to the newly-created database.
  connect(superuser.copy(databaseName = test.databaseName)).use { postgresqlClient ->
    postgresqlClient.withConnection {
      execute("DROP SCHEMA IF EXISTS public CASCADE")
      execute("CREATE SCHEMA public")
      execute("GRANT ALL ON SCHEMA public TO ${test.user}")
    }
  }
}

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

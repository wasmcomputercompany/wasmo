package com.wasmo.sql

import io.vertx.pgclient.PgException

/**
 * Initializes the OS database.
 */
class OsDatabaseInitializer(
  private val clientFactory: PostgresqlClient.Factory,
  private val superuser: PostgresqlAddress,
  private val address: PostgresqlAddress,
) {
  suspend fun initialize() {
    clientFactory.connect(superuser).use { client ->
      try {
        client.withConnection {
          execute("CREATE DATABASE ${address.databaseName} WITH ENCODING = 'UTF8'")
          execute("CREATE USER ${address.user} WITH PASSWORD '${address.password}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
          execute("GRANT ALL PRIVILEGES ON DATABASE ${address.databaseName} TO ${address.user}")
        }
      } catch (_: PgException) {
        // Assume this database exists.
      }
    }
  }

  /** Drop the database. This authenticates as superuser to the OS database. */
  suspend fun dangerouslyClearSchema() {
    clientFactory.connect(superuser.copy(databaseName = address.databaseName)).use { client ->
      client.withConnection {
        execute("DROP SCHEMA IF EXISTS public CASCADE")
        execute("CREATE SCHEMA public")
        execute("GRANT ALL ON SCHEMA public TO ${address.user}")
      }
    }
  }
}

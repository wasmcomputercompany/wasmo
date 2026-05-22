package com.wasmo.sql

import io.vertx.pgclient.PgException

/**
 * Initializes the OS database.
 */
class OsDatabaseInitializer(
  private val clientFactory: PostgresqlClient.Factory,
  private val address: WasmoPostgresqlConfig,
) {
  suspend fun initialize() {
    clientFactory.connect(address.admin).use { client ->
      try {
        client.withConnection {
          execute("CREATE DATABASE ${address.osDatabaseName} WITH ENCODING = 'UTF8'")
          execute("CREATE USER ${address.osUser} WITH PASSWORD '${address.osPassword}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
          execute("GRANT ALL PRIVILEGES ON DATABASE ${address.osDatabaseName} TO ${address.osUser}")
        }
      } catch (_: PgException) {
        // Assume this database exists.
      }
    }
  }

  /** Drop the database. This authenticates as superuser to the OS database. */
  suspend fun dangerouslyClearSchema() {
    clientFactory.connect(address.adminToOsDatabase).use { client ->
      client.withConnection {
        execute("DROP SCHEMA IF EXISTS public CASCADE")
        execute("CREATE SCHEMA public")
        execute("GRANT ALL ON SCHEMA public TO ${address.osUser}")
      }
    }
  }
}

package com.wasmo.sql

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.vertx.pgclient.PgException

@Inject
@SingleIn(OsScope::class)
class RealOsDatabaseInitializer(
  private val clientFactory: PostgresqlClient.Factory,
  private val config: WasmoPostgresqlConfig,
) : OsDatabaseInitializer {
  override suspend fun initialize() {
    clientFactory.connect(config.admin).use { client ->
      try {
        client.withConnection {
          execute("CREATE DATABASE ${config.osDatabaseName} WITH ENCODING = 'UTF8'")
          execute("CREATE USER ${config.osUser} WITH PASSWORD '${config.osPassword}' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT")
          execute("GRANT ALL PRIVILEGES ON DATABASE ${config.osDatabaseName} TO ${config.osUser}")
          execute("ALTER DATABASE ${config.osDatabaseName} OWNER TO ${config.osUser}")
        }
      } catch (_: PgException) {
        // Assume this database exists.
      }
    }
  }

  /** Drop the database. This authenticates as superuser to the OS database. */
  suspend fun dangerouslyClearSchema() {
    clientFactory.connect(config.adminToOsDatabase).use { client ->
      client.withConnection {
        execute("DROP SCHEMA IF EXISTS public CASCADE")
        execute("CREATE SCHEMA public")
        execute("GRANT ALL ON SCHEMA public TO ${config.osUser}")
      }
    }
  }
}

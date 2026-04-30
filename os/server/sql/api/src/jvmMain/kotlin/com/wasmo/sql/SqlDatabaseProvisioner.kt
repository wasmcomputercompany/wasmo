package com.wasmo.sql

import com.wasmo.identifiers.DatabaseSlug

/**
 * Finds the address to a database, provisioning it if necessary. Unlike a basic factory classes,
 * this calls out to the Postgresql server to set up a new database.
 */
interface SqlDatabaseProvisioner {
  suspend fun getOrProvision(
    databaseSlug: DatabaseSlug,
  ): PostgresqlAddress
}

package com.wasmo.sql

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.DatabaseSlug

interface SqlDatabaseFactory {
  suspend fun getOrCreate(
    databaseSlug: DatabaseSlug,
  ): PostgresqlAddress
}

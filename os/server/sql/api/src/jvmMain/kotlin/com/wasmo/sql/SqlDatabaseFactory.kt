package com.wasmo.sql

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.support.closetracker.CloseListener
import wasmo.sql.SqlDatabase

interface SqlDatabaseFactory {
  suspend fun create(
    appSlug: AppSlug,
    computerSlug: ComputerSlug,
    name: String,
    closeListener: CloseListener,
  ): SqlDatabase
}

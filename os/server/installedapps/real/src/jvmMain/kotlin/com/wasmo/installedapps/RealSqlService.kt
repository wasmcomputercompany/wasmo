@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.installedapps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.sql.SqlDatabaseFactory
import com.wasmo.support.closetracker.CloseTracker
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.ExperimentalUuidApi
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlService

/**
 * Provisions databases for applications.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class RealSqlService(
  private val computerSlug: ComputerSlug,
  private val appSlug: AppSlug,
  private val sqlDatabaseFactory: SqlDatabaseFactory,
) : SqlService {
  private val closeTracker = CloseTracker()

  override suspend fun getOrCreate(name: String): SqlDatabase {
    return closeTracker.track { closeListener ->
      sqlDatabaseFactory.create(
        appSlug,
        computerSlug,
        name,
        closeListener,
      )
    }
  }

  override fun close() {
    closeTracker.closeAll()
  }
}

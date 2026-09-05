package com.wasmo.sql

import com.wasmo.events.EventListener
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.support.closetracker.CloseTracker
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlService

/**
 * Provisions databases for applications.
 */
@Inject
@SingleIn(InstalledAppScope::class)
class RealSqlService(
  private val sqlDatabaseProvisioner: SqlDatabaseProvisioner,
  private val vertxRowMetadataHack: VertxRowMetadataHack,
  private val eventListener: EventListener,
) : SqlService {
  private val closeTracker = CloseTracker()

  override suspend fun getOrCreate(name: String): SqlDatabase {
    val databaseSlug = DatabaseSlug(name)
    return closeTracker.track { closeListener ->
      val databaseAddress = sqlDatabaseProvisioner.getOrProvision(databaseSlug)
      val client = PostgresqlClient.Factory(vertxRowMetadataHack, eventListener)
        .connect(databaseAddress)

      RealSqlDatabase(
        client = client,
        closeListener = closeListener,
        eventListener = eventListener,
      )
    }
  }

  override fun close() {
    closeTracker.closeAll()
  }
}

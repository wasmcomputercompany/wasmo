package com.wasmo.sql

import com.wasmo.db.installedapps.insertInstalledAppDatabase
import com.wasmo.db.installedapps.selectInstalledAppDatabaseByInstalledAppDbSlug
import com.wasmo.events.EventListener
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.identifiers.Secret
import com.wasmo.support.closetracker.trackAndClose
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction
import wasmox.sql.withConnection

@Inject
@SingleIn(InstalledAppScope::class)
class RealSqlDatabaseProvisioner(
  private val computerSlug: ComputerSlug,
  private val appSlug: AppSlug,
  private val appId: InstalledAppId,
  private val postgresqlConfig: WasmoPostgresqlConfig,
  private val provisioningDb: ProvisioningDb,
  private val osDb: SqlDatabase,
  private val appDatabasePasswordSource: AppDatabasePasswordSource,
  private val clock: Clock,
  private val eventListener: EventListener,
) : SqlDatabaseProvisioner {
  override suspend fun getOrProvision(
    databaseSlug: DatabaseSlug,
  ): PostgresqlAddress {
    val databaseName = when {
      databaseSlug.isEmpty() -> "${postgresqlConfig.appPrefix}_app_${computerSlug}_${appSlug}"
      else -> "${postgresqlConfig.appPrefix}_app_${computerSlug}_${appSlug}_$databaseSlug"
    }
    val appUsername = "${databaseName}_user"
    val appPassword = appDatabasePasswordSource.retrievePassword(databaseSlug)

    val existingAppDatabase = osDb.transaction {
      selectInstalledAppDatabaseByInstalledAppDbSlug(
        installedAppId = appId,
        slug = databaseSlug,
      )
    }
    if (existingAppDatabase == null) {
      provisionNewAppDatabase(
        appUsername = appUsername,
        databaseName = databaseName,
        databaseSlug = databaseSlug,
        databasePassword = appPassword
      )
    }

    return postgresqlConfig.appDatabase(
      user = appUsername,
      password = appPassword,
      databaseName = databaseName,
    )
  }

  private suspend fun provisionNewAppDatabase(
    appUsername: String,
    databaseName: String,
    databaseSlug: DatabaseSlug,
    databasePassword: Secret
  ) {

    // Provision with cluster level commands
    provisioningDb.provisioningDb.withConnection {
      createBareAppUser(appUsername, databasePassword)
      createAppDatabase(databaseName)
      restrictAppDatabaseAccess(databaseName)
      grantAccessToAppUser(databaseName, appUsername)
    }

    // Log into the database as the provisioning owner to set up the app user permissions.
    trackAndClose { closeTracker ->
      val appDb = closeTracker.track { closeListener ->
        val client = PostgresqlClient.Factory(eventListener)
          .connect(postgresqlConfig.adminToAppDatabase(databaseName))
        RealSqlDatabase(
          client = client,
          closeListener = closeListener,
          eventListener = eventListener,
        )
      }

      appDb.withConnection {
        revokePublicSchemaAccess()
        grantSchemaAccessToAppUser(appUsername)
      }
    }

    val now = clock.now()

    // TODO: Figure out the path forward when this partially breaks.
    // Save this to the database
    val installedAppDatabaseId = osDb.transaction {
      insertInstalledAppDatabase(
        installedAppId = appId,
        slug = databaseSlug,
        createdAt = now,
        version = 1L,
      )
    }
  }
}

package com.wasmo.sql

import com.wasmo.db.installedapps.DbInstalledAppDatabase
import com.wasmo.db.installedapps.insertInstalledAppDatabase
import com.wasmo.db.installedapps.selectInstalledAppDatabaseByInstalledAppDbSlug
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.support.closetracker.CloseTracker
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okio.ByteString.Companion.encodeUtf8
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction
import wasmox.sql.withConnection

@Inject
@SingleIn(InstalledAppScope::class)
class RealSqlDatabaseFactory(
  private val computerSlug: ComputerSlug,
  private val appSlug: AppSlug,
  private val appId: InstalledAppId,
  private val provisioningDb: ProvisioningDb,
  private val osDb: SqlDatabase,
  private val clock: Clock
) : SqlDatabaseFactory {
  override suspend fun getOrCreate(
    databaseSlug: DatabaseSlug,
  ): PostgresqlAddress {
    val databaseName = when {
      databaseSlug.isEmpty() -> "app_${computerSlug}_${appSlug}"
      else -> "app_${computerSlug}_${appSlug}_$databaseSlug"
    }
    val appUsername = "${databaseName}_user"

    var installedAppDatabase = osDb.transaction {
      selectInstalledAppDatabaseByInstalledAppDbSlug(
        installedAppId = appId,
        slug = databaseSlug
      )
    }

    if (installedAppDatabase == null){
      installedAppDatabase = provisionNewAppDatabase(
        appUsername,
        databaseName,
        databaseSlug,
      )
    }

    // TODO: Clean up secrets and settings before invoking
    val appPassword = installedAppDatabase.credential.utf8()
    return provisioningDb.address.copy(
      user = appUsername,
      databaseName = databaseName,
      password = appPassword,
    )
  }

  private suspend fun provisionNewAppDatabase(
    appUsername: String,
    databaseName: String,
    databaseSlug: DatabaseSlug,
  ): DbInstalledAppDatabase {
    // TODO: Generate and encrypt this:
    val appUserPassword = "app-password"

    // Provision with cluster level commands
    provisioningDb.provisioningDb.withConnection {
      // Creates a bare app user.
      contextOf<SqlConnection>().execute(
        sql = """
          CREATE USER $appUsername
          WITH PASSWORD '$appUserPassword'
          NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT
        """,
        // TODO: MAKE SURE THIS IS SECURE (bind parameters?)
      )

      // Creates the requested App database.
      contextOf<SqlConnection>().execute(
        sql = "CREATE DATABASE $databaseName WITH ENCODING = 'UTF8'",
      )

      // Remove all access, except for the owner.
      contextOf<SqlConnection>().execute(
        sql = "REVOKE CONNECT, TEMPORARY ON DATABASE $databaseName FROM PUBLIC",
      )
      // Add access for the app user.
      contextOf<SqlConnection>().execute(
        sql = "GRANT CONNECT ON DATABASE $databaseName TO $appUsername",
      )
    }

    // Log into the database as the provisioning owner to set up the app user permissions.
    val closeTracker = CloseTracker()
    try {
      val appDb = closeTracker.track { closeListener ->
        val appDatabaseAddress = provisioningDb.address.copy(databaseName = databaseName)
        val client = PostgresqlClient.Factory()
          .connect(appDatabaseAddress)
        RealSqlDatabase(
          client = client,
          closeListener = closeListener,
        )
      }

      appDb.withConnection {
        // Remove all public schema access.
        contextOf<SqlConnection>().execute(
          sql = "REVOKE ALL ON SCHEMA public FROM PUBLIC",
        )
        // Add limited schema access to the app user.
        contextOf<SqlConnection>().execute(
          sql = "GRANT USAGE, CREATE ON SCHEMA public TO $appUsername",
        )
      }
    } finally {
      closeTracker.closeAll()
    }

    // TODO: Figure out the path forward when this partially breaks.
    // Save this to the database
    osDb.transaction {
      insertInstalledAppDatabase(
        installedAppId = appId,
        slug = databaseSlug,
        createdAt = clock.now(),
        version = 1L,
        credential = appUserPassword.encodeUtf8(), // TODO: Fix
      )
    }
    // Check that the record now exists.
    val installedAppDatabase = osDb.transaction {
      selectInstalledAppDatabaseByInstalledAppDbSlug(
        installedAppId = appId,
        slug = databaseSlug,
      )
    }

    checkNotNull(
      installedAppDatabase,
      {
        "Failed to save new database information for $databaseName."
      },
    )
    return installedAppDatabase
  }
}

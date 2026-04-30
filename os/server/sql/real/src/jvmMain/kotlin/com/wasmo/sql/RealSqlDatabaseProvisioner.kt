package com.wasmo.sql

import com.wasmo.db.installedapps.DbInstalledAppDatabase
import com.wasmo.db.installedapps.insertInstalledAppDatabase
import com.wasmo.db.installedapps.selectInstalledAppDatabaseByInstalledAppDbSlug
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.support.closetracker.trackAndClose
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction
import wasmox.sql.withConnection

@Inject
@SingleIn(InstalledAppScope::class)
class RealSqlDatabaseProvisioner(
  private val computerSlug: ComputerSlug,
  private val appSlug: AppSlug,
  private val appId: InstalledAppId,
  private val provisioningDb: ProvisioningDb,
  private val osDb: SqlDatabase,
  private val clock: Clock,
) : SqlDatabaseProvisioner {
  override suspend fun getOrProvision(
    databaseSlug: DatabaseSlug,
  ): PostgresqlAddress {
    val databaseName = when {
      databaseSlug.isEmpty() -> "app_${computerSlug}_${appSlug}"
      else -> "app_${computerSlug}_${appSlug}_$databaseSlug"
    }
    val appUsername = "${databaseName}_user"

    val installedAppDatabase = osDb.transaction {
      selectInstalledAppDatabaseByInstalledAppDbSlug(
        installedAppId = appId,
        slug = databaseSlug,
      )
    } ?: provisionNewAppDatabase(
      appUsername = appUsername,
      databaseName = databaseName,
      databaseSlug = databaseSlug,
    )

    // TODO: Clean up secrets and settings before invoking
    val appPassword = decryptAppPassword(installedAppDatabase)
    return PostgresqlAddress(
      hostname = provisioningDb.address.hostname,
      ssl = provisioningDb.address.ssl,
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
    val plaintextPassword = generateAppPassword()
    val encryptedPassword = encryptAppPassword(plaintextPassword)

    // Provision with cluster level commands
    provisioningDb.provisioningDb.withConnection {
      createBareAppUser(appUsername, plaintextPassword)
      createAppDatabase(databaseName)
      restrictAppDatabaseAccess(databaseName)
      grantAccessToAppUser(databaseName, appUsername)
    }

    // Log into the database as the provisioning owner to set up the app user permissions.
    trackAndClose { closeTracker ->
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
        credential = encryptedPassword,
      )
    }

    return DbInstalledAppDatabase(
      id = installedAppDatabaseId,
      installedAppId = appId,
      slug = databaseSlug,
      createdAt = now,
      version = 1L,
      credential = encryptedPassword,
    )
  }

  private fun generateAppPassword(): String {
    return "app-password"
  }

  // TODO: use Vault.
  private fun encryptAppPassword(appPassword: String): ByteString {
    return appPassword.encodeUtf8()
  }

  // TODO: use Vault.
  private fun decryptAppPassword(installedAppDatabase: DbInstalledAppDatabase): String {
    return installedAppDatabase.credential.utf8()
  }
}

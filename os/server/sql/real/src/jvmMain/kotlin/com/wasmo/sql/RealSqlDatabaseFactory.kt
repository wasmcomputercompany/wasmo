package com.wasmo.sql

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.OsScope
import com.wasmo.support.closetracker.CloseListener
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase

/** Between 1 and 15 letters or digits, and the first is not a digit. */
val DatabaseNameRegex = Regex("[a-z][a-z0-9]{0,14}")

@Inject
@SingleIn(OsScope::class)
class RealSqlDatabaseFactory(
  private val provisioningDb: ProvisioningDb,
) : SqlDatabaseFactory {
  override suspend fun create(
    appSlug: AppSlug,
    computerSlug: ComputerSlug,
    name: String,
    closeListener: CloseListener,
  ): SqlDatabase {
    require(name.isEmpty() || DatabaseNameRegex.matches(name)) {
      "unexpected database name: $name"
    }

    val databaseName = when {
      name.isEmpty() -> "app_${computerSlug}_${appSlug}"
      else -> "app_${computerSlug}_${appSlug}_$name"
    }

    provisioningDb.provisioningDb.withConnection {
      contextOf<SqlConnection>().execute(
        sql = "CREATE DATABASE $databaseName WITH ENCODING = 'UTF8'",
      )
    }

    // TODO: figure out secrets and settings before invoking
    // TODO: provision this database if it doesn't exist
    //            If I do I have to get the special database creation/deletion account.
    //            Create the user for this database too.
    val postgresqlAddress = provisioningDb.address.copy(
      databaseName = databaseName,
    )

    return RealSqlDatabase(
      client = PostgresqlClient(postgresqlAddress),
      closeListener = closeListener,
    )
  }
}

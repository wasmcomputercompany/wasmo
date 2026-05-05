package com.wasmo.db

import com.wasmo.db.schemaversion.getOrCreateSchemaVersion
import com.wasmo.db.schemaversion.setSchemaVersion
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import wasmo.sql.SqlConnection
import wasmo.sql.SqlException

/**
 * Applies migrations from resources.
 *
 * This function is agnostic of schema version metadata. It's the caller's
 * responsibility to check and update the schema version record before/after
 * the call.
 *
 * Migration files must be named like `v1__Account.sql`. They're applied in number order.
 */
context(sqlConnection: SqlConnection)
private suspend fun migrate(
  oldVersion: Long = 0L,
  newVersion: Long = CURRENT_SCHEMA_VERSION,
) {
  val migrationPaths = FileSystem.RESOURCES.list("/migrations".toPath())

  val migrations = migrationPaths.map { path ->
    val match = MigrationNameRegex.matchEntire(path.name)
      ?: error("unexpected migrations path: $path")
    Migration(match.groupValues[1].toInt(), path)
  }

  for (migration in migrations.sortedBy { it.version }) {
    if (migration.version !in (oldVersion + 1)..newVersion) continue

    val migrationSql = FileSystem.RESOURCES.read(migration.path) {
      readUtf8()
    }
    sqlConnection.execute(migrationSql)
  }
}

context(sqlConnection: SqlConnection)
suspend fun ensureSchemaVersion(targetVersion: Long = CURRENT_SCHEMA_VERSION) {
  val oldVersion = getOrCreateSchemaVersion().version
  if (oldVersion > targetVersion) {
    throw SqlException("DB schema downgrade not supported: $oldVersion -> $targetVersion")
  } else if (oldVersion < targetVersion) {
    migrate(oldVersion, targetVersion)
    setSchemaVersion(version = targetVersion)
  }
}

private val MigrationNameRegex = Regex("""v(\d+)__.*\.sql""")
const val CURRENT_SCHEMA_VERSION = 1L

private data class Migration(
  val version: Int,
  val path: Path,
)

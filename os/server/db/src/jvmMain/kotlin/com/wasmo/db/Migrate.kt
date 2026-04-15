package com.wasmo.db

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import wasmo.sql.SqlConnection

/**
 * Applies migrations from resources.
 *
 * Migration files must be named like `v1__Account.sql`. They're applied in number order.
 */
context(sqlConnection: SqlConnection)
suspend fun migrate(
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

private val MigrationNameRegex = Regex("""v(\d+)__.*\.sql""")
const val CURRENT_SCHEMA_VERSION = 1L

private data class Migration(
  val version: Int,
  val path: Path,
)

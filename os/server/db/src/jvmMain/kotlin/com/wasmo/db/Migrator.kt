package com.wasmo.db

import com.wasmo.db.schemaversion.getOrCreateSchemaVersion
import com.wasmo.db.schemaversion.setSchemaVersion
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import wasmo.sql.SqlConnection
import wasmo.sql.SqlException
import wasmox.sql.SqlTransaction

interface Migrator {

  /**
   * Upgrades the DB schema to the given version, if needed.
   *
   * @param targetSchema the schema version to upgrade to, or null to upgrade to the latest version.
   */
  context(sqlTransaction: SqlTransaction)
  suspend fun ensureSchemaVersion(targetSchema: NamedSchema? = null)
}

@Inject
@SingleIn(OsScope::class)
open class RealMigrator(
  private val customPostUpgradeSteps: Map<Long, suspend context(SqlTransaction) () -> Unit> = emptyMap(),
) : Migrator {

  /**
   * Applies migrations from resources.
   *
   * This function is agnostic of schema version metadata. It's the caller's
   * responsibility to check and update the schema version record before/after
   * the call.
   *
   * Migration files must be named like `v1__Account.sql`. They're applied in number order.
   */
  context(sqlTransaction: SqlTransaction)
  private suspend fun migrate(
    oldVersion: Long,
    newVersion: Long,
  ) {
    val migrationPaths = FileSystem.RESOURCES.list("/migrations".toPath())

    val migrations = migrationPaths.map { path ->
      val match = MigrationNameRegex.matchEntire(path.name)
        ?: error("unexpected migrations path: $path")
      Migration(match.groupValues[1].toLong(), path)
    }.groupBy { it.version }

    for (version in (oldVersion + 1)..newVersion) {
      for (migration in migrations[version] ?: emptyList()) {
        val migrationSql = FileSystem.RESOURCES.read(migration.path) {
          readUtf8()
        }
        sqlTransaction.sqlConnection.execute(migrationSql)
        customPostUpgradeSteps[migration.version]?.invoke(sqlTransaction)
      }
    }
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun ensureSchemaVersion(
    targetSchema: NamedSchema?,
  ) {
    val targetVersion = (targetSchema ?: CURRENT_SCHEMA_VERSION).version
    val oldVersion: Long = getOrCreateSchemaVersion().version
    if (oldVersion > targetVersion) {
      throw SqlException("DB schema downgrade not supported: $oldVersion -> $targetVersion")
    } else if (oldVersion < targetVersion) {
      migrate(oldVersion, targetVersion)
      setSchemaVersion(version = targetVersion)
    }
  }
}

private val MigrationNameRegex = Regex("""v(\d+)__.*\.sql""")
private data class Migration(
  val version: Long,
  val path: Path,
)

private val CURRENT_SCHEMA_VERSION = NamedSchema.entries.maxBy { it.version }

/**
 * Enum instances to make schema revision versions more readable.
 */
enum class NamedSchema(val version: Long) {
  EMPTY(0),
  BASE(1),
  ABSURD(2),
  ABSURD_QUEUE(3),
  USERNAME(4),
}

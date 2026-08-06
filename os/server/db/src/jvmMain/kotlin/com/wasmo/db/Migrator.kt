package com.wasmo.db

import com.wasmo.common.logging.Logger
import com.wasmo.db.schemaversion.DbSchemaVersion
import com.wasmo.db.schemaversion.getOrCreateSchemaVersion
import com.wasmo.db.schemaversion.setSchemaVersion
import com.wasmo.identifiers.OsScope
import com.wasmo.sql.WasmoPostgresqlConfig
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.SingleIn
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath
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

@AssistedInject
@SingleIn(OsScope::class)
open class RealMigrator(
  @Assisted private val customPostUpgradeSteps: Map<Long, suspend context(SqlTransaction) () -> Unit> = emptyMap(),
  private val address: WasmoPostgresqlConfig,
  private val logger: Logger,
) : Migrator {
  @AssistedFactory
  fun interface Factory {
    fun create(customPostUpgradeSteps: Map<Long, suspend context(SqlTransaction) () -> Unit>): RealMigrator
  }

  private suspend fun readMigrationSql(): Map<Long, List<Migration>> {
    // okio FileSystem.list() sorts results, ensures deterministic order for hash and execution.
    val migrationPaths = FileSystem.RESOURCES.list("/migrations".toPath())

    val migrations = migrationPaths.map { path ->
      val match = MigrationNameRegex.matchEntire(path.name)
        ?: error("unexpected migrations path: $path")
      val migrationSql = FileSystem.RESOURCES.read(path) {
        readUtf8()
      }
      Migration(match.groupValues[1].toLong(), migrationSql)
    }.groupBy { it.version }
    return migrations
  }

  private fun migrationHashUpToVersion(
    migrations: Map<Long, List<Migration>>,
    version: Long,
  ): ByteString = migrations.filterKeys { v -> v <= version }.toString().encodeUtf8().sha256()

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
    migrations: Map<Long, List<Migration>>,
    oldVersion: Long,
    newVersion: Long,
  ) {
    for (version in (oldVersion + 1)..newVersion) {
      for (migration in migrations[version] ?: emptyList()) {
        sqlTransaction.execute(migration.sql)
        customPostUpgradeSteps[migration.version]?.invoke(sqlTransaction)
      }
    }
  }


  context(sqlTransaction: SqlTransaction)
  override suspend fun ensureSchemaVersion(
    targetSchema: NamedSchema?,
  ) {
    val targetVersion = (targetSchema ?: CURRENT_SCHEMA_VERSION).version
    require(targetVersion >= 0) {
      "Invalid targetVersion: $targetVersion"
    }
    val migrations = readMigrationSql()
    val versionZeroMigrationHash = migrationHashUpToVersion(emptyMap(), 0)
    var oldDbVersion: DbSchemaVersion = getOrCreateSchemaVersion(versionZeroMigrationHash)
    var oldVersion = oldDbVersion.version
    val expectedMigrationHash = migrationHashUpToVersion(migrations, oldVersion)
    val shouldWipeDb = expectedMigrationHash != oldDbVersion.migrationHash
    if (shouldWipeDb) {
      logger.info("DB schema redefinition detected at version $oldVersion, resetting to version 0")
      for (sql in listOf(
        // same commands as in OsDatabaseInitializer.dangerouslyClearSchema()
        "DROP SCHEMA IF EXISTS public CASCADE",
        "CREATE SCHEMA public",
        "GRANT ALL ON SCHEMA public TO ${address.osUser}",

        // same commands as in AbsurdTesting.dangerouslyClearAbsurdSchema
        "DROP SCHEMA IF EXISTS absurd CASCADE",
        "CREATE SCHEMA absurd",
        "GRANT ALL ON SCHEMA absurd TO postgres",
        "GRANT ALL ON SCHEMA absurd TO public"
      )) {
        sqlTransaction.execute(sql)
      }

      oldDbVersion = getOrCreateSchemaVersion(versionZeroMigrationHash)
      oldVersion = oldDbVersion.version
      check(oldVersion == 0L) {
        "Unexpected DB schema version after DB wipe: $oldVersion"
      }
    }

    if (oldVersion > targetVersion) {
      throw IllegalStateException("DB schema downgrade not supported: $oldVersion -> $targetVersion")
    } else if (oldVersion < targetVersion) {
      logger.info("DB schema migration: version $oldVersion -> $targetVersion")
      val targetMigrationHash = migrationHashUpToVersion(migrations, targetVersion)
      migrate(migrations, oldVersion, targetVersion)
      setSchemaVersion(version = targetVersion, migrationHash = targetMigrationHash)
    }
  }
}

private val MigrationNameRegex = Regex("""v(\d+)__.*\.sql""")
private data class Migration(
  val version: Long,
  val sql: String,
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
  PASSWORD(5),
}

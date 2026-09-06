package com.wasmo.db.schemaversion

import com.wasmo.db.getSchemaVersionId
import okio.ByteString
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.SqlTransaction
import wasmox.sql.single

// id (primary key) of the one and only row that's ever in the table
private const val DB_SCHEMA_VERSION_ID = 1

context(sqlTransaction: SqlTransaction)
suspend fun getOrCreateSchemaVersion(versionZeroMigrationHash: ByteString): DbSchemaVersion {
  ensureSchemaVersionStored(versionZeroMigrationHash)
  return getSchemaVersion()
}

context(connection: SqlConnection)
suspend fun getSchemaVersion(): DbSchemaVersion {
  val rowIterator = connection.executeQuery(
    "SELECT id, version, migration_hash FROM DatabaseSchemaVersion WHERE id=$1"
  ) {
    bindS32(0, DB_SCHEMA_VERSION_ID)
  }
  return rowIterator.single() {
    getSchemaVersion()
  }
}

context(connection: SqlConnection)
suspend fun setSchemaVersion(version: Long, migrationHash: ByteString) {
  val rowIterator = connection.execute(
    "UPDATE DatabaseSchemaVersion SET version=$1, migration_hash=$2 WHERE id=$3"
  ) {
    bindS64(0, version)
    bindBytes(1, migrationHash)
    bindS32(2, DB_SCHEMA_VERSION_ID)
  }
}

context(transaction: SqlTransaction)
private suspend fun ensureSchemaVersionStored(versionZeroMigrationHash: ByteString) {
  // execute() each statement individually for finer-grained errors and to not
  // depend on driver support for multiple statements per execute().
  transaction.execute(
    // Use String interpolation because $1 arg placeholders aren't supported for CREATE TABLE.
    // https://www.postgresql.org/docs/current/sql-prepare.html
    """
    CREATE TABLE IF NOT EXISTS DatabaseSchemaVersion (
      id BIGINT NOT NULL PRIMARY KEY DEFAULT $DB_SCHEMA_VERSION_ID,
      version BIGINT NOT NULL,
      -- hash of the Map of migration SQLs up to and including version.
      migration_hash BYTEA NOT NULL
    )
    """
  )
  transaction.execute("ALTER TABLE DatabaseSchemaVersion DROP CONSTRAINT IF EXISTS only_one_row")
  // Use String interpolation because $1 arg placeholders aren't supported for ALTER TABLE.
  // https://www.postgresql.org/docs/current/sql-prepare.html
  transaction.execute("ALTER TABLE DatabaseSchemaVersion ADD CONSTRAINT only_one_row CHECK (id = $DB_SCHEMA_VERSION_ID)")
  transaction.execute(
    """
    INSERT INTO DatabaseSchemaVersion (id, version, migration_hash)
      VALUES ($1, $2, $3)
      ON CONFLICT (id) DO NOTHING;
    """
  ) {
    bindS32(0, DB_SCHEMA_VERSION_ID) // id
    bindS32(1, 0) // version
    bindBytes(2, versionZeroMigrationHash) // migration_hash
  }
}

private fun SqlRow.getSchemaVersion(): DbSchemaVersion = DbSchemaVersion(
  id = getSchemaVersionId(0),
  version = getS64(1)!!,
  migrationHash = getBytes(2)!!
)

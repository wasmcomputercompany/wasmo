package com.wasmo.db.schemaversion

import com.wasmo.db.getSchemaVersionId
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.single

// id (primary key) of the one and only row that's ever in the table
private const val DB_SCHEMA_VERSION_ID = 1

context(connection: SqlConnection)
suspend fun getOrCreateSchemaVersion(): DbSchemaVersion {
  ensureSchemaVersionStored()
  return getSchemaVersion()
}

context(connection: SqlConnection)
suspend fun getSchemaVersion(): DbSchemaVersion {
  val rowIterator = connection.executeQuery(
    "SELECT id, version FROM DatabaseSchemaVersion WHERE id=$1"
  ) {
    bindS32(0, DB_SCHEMA_VERSION_ID)
  }
  return rowIterator.single() {
    getSchemaVersion()
  }
}

context(connection: SqlConnection)
suspend fun setSchemaVersion(version: Long) {
  val rowIterator = connection.execute(
    "UPDATE DatabaseSchemaVersion SET version=$1 WHERE id=$2"
  ) {
    bindS64(0, version)
    bindS32(1, DB_SCHEMA_VERSION_ID)
  }
}

context(connection: SqlConnection)
private suspend fun ensureSchemaVersionStored() {
  // execute() each statement individually for finer-grained errors and to not
  // depend on driver support for multiple statements per execute().
  connection.execute(
    // Use String interpolation because $1 arg placeholders aren't supported for CREATE TABLE.
    // https://www.postgresql.org/docs/current/sql-prepare.html
    """
    CREATE TABLE IF NOT EXISTS DatabaseSchemaVersion (
      id INTEGER NOT NULL PRIMARY KEY DEFAULT $DB_SCHEMA_VERSION_ID,
      version INTEGER NOT NULL
    )
    """
  )
  connection.execute("ALTER TABLE DatabaseSchemaVersion DROP CONSTRAINT IF EXISTS only_one_row")
  // Use String interpolation because $1 arg placeholders aren't supported for ALTER TABLE.
  // https://www.postgresql.org/docs/current/sql-prepare.html
  connection.execute("ALTER TABLE DatabaseSchemaVersion ADD CONSTRAINT only_one_row CHECK (id = $DB_SCHEMA_VERSION_ID)")
  connection.execute(
    """
    INSERT INTO DatabaseSchemaVersion (id, version)
      VALUES ($1, 0)
      ON CONFLICT (id) DO NOTHING;
    """
  ) {
    bindS32(0, DB_SCHEMA_VERSION_ID)
  }
}

private fun SqlRow.getSchemaVersion(): DbSchemaVersion = DbSchemaVersion(
  id = getSchemaVersionId(0),
  version = getS64(1)!!,
)

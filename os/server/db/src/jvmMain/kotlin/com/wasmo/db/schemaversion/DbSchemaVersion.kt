package com.wasmo.db.schemaversion

import com.wasmo.identifiers.SchemaVersionId
import okio.ByteString

data class DbSchemaVersion(
  val id: SchemaVersionId,
  val version: Long,
  val migrationHash: ByteString,
)


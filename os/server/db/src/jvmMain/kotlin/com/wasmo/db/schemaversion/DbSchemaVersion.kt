package com.wasmo.db.schemaversion

import com.wasmo.identifiers.SchemaVersionId

data class DbSchemaVersion(
  val id: SchemaVersionId,
  val version: Long,
)


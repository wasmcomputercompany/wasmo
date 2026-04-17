package com.wasmo.sql

import wasmo.sql.SqlDatabase

data class PostgresqlAddress(
  val user: String,
  val password: String,
  val hostname: String,
  val databaseName: String,
  val ssl: Boolean,
)

class ProvisioningDb(
  val address: PostgresqlAddress,
  val provisioningDb: SqlDatabase,
)

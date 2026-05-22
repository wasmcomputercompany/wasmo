package com.wasmo.sql

import wasmo.sql.SqlDatabase

class ProvisioningDb(
  val address: PostgresqlAddress,
  val provisioningDb: SqlDatabase,
)

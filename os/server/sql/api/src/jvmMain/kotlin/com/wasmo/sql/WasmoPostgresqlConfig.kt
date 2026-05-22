package com.wasmo.sql

data class WasmoPostgresqlConfig(
  val hostname: String,
  val ssl: Boolean,
  val adminUser: String,
  val adminPassword: String,
  val adminDatabaseName: String,
  val osUser: String,
  val osPassword: String,
  val osDatabaseName: String,
) {
  /** Connect to the database to provision application databases. */
  val admin: PostgresqlAddress
    get() = PostgresqlAddress(
      hostname = hostname,
      ssl = ssl,
      user = adminUser,
      password = adminPassword,
      databaseName = adminDatabaseName,
    )

  /** The OS's own database. */
  val os: PostgresqlAddress
    get() = PostgresqlAddress(
      hostname = hostname,
      ssl = ssl,
      user = osUser,
      password = osPassword,
      databaseName = osDatabaseName,
    )

  /** Authenticates as admin to the OS database. */
  val adminToOsDatabase: PostgresqlAddress
    get() = admin.copy(databaseName = osDatabaseName)
}

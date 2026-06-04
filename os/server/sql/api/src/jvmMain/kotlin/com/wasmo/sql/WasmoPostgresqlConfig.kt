package com.wasmo.sql

import com.wasmo.identifiers.Secret

data class WasmoPostgresqlConfig(
  val hostname: String,
  val port: Int = 5432,
  val ssl: Boolean = false,
  val adminUser: String,
  val adminPassword: Secret,
  val adminDatabaseName: String,
  val osUser: String,
  val osPassword: Secret,
  val osDatabaseName: String,
) {
  /** Connect to the database to provision application databases. */
  val admin: PostgresqlAddress
    get() = PostgresqlAddress(
      hostname = hostname,
      port = port,
      ssl = ssl,
      user = adminUser,
      password = adminPassword,
      databaseName = adminDatabaseName,
    )

  /** The OS's own database. */
  val os: PostgresqlAddress
    get() = PostgresqlAddress(
      hostname = hostname,
      port = port,
      ssl = ssl,
      user = osUser,
      password = osPassword,
      databaseName = osDatabaseName,
    )

  /** Authenticates as admin to the OS database. */
  val adminToOsDatabase: PostgresqlAddress
    get() = admin.copy(databaseName = osDatabaseName)

  fun appDatabase(
    user: String,
    password: Secret,
    databaseName: String,
  ) = PostgresqlAddress(
    hostname = hostname,
    port = port,
    ssl = ssl,
    user = user,
    password = password,
    databaseName = databaseName,
  )

  fun adminToAppDatabase(
    databaseName: String,
  ) = admin.copy(
    databaseName = databaseName,
  )
}

package com.wasmo.sql

data class PostgresqlAddress(
  val hostname: String,
  val port: Int = 5432,
  val ssl: Boolean = false,
  val user: String,
  val password: String,
  val databaseName: String,
) {
  /** This is for debugging only. Note that this omits the password. */
  override fun toString() = buildString {
    append("$user@$hostname")
    if (port != 5432) {
      append(":$port")
    }
    append("/$databaseName")
  }
}

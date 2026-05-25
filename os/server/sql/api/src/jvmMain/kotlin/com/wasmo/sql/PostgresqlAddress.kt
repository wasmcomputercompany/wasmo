package com.wasmo.sql

data class PostgresqlAddress(
  val hostname: String,
  val port: Int = 5432,
  val ssl: Boolean = false,
  val user: String,
  val password: String,
  val databaseName: String,
) {
  /** Note that this omits the password. */
  override fun toString() = "$user@$hostname/$databaseName"
}

package com.wasmo.sql

data class PostgresqlAddress(
  val hostname: String,
  val ssl: Boolean,
  val user: String,
  val password: String,
  val databaseName: String,
)

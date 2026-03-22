package com.wasmo.sql

data class PostgresqlAddress(
  val user: String,
  val password: String,
  val hostname: String,
  val databaseName: String,
  val ssl: Boolean,
)

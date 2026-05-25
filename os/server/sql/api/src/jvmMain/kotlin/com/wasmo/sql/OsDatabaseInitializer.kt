package com.wasmo.sql

interface OsDatabaseInitializer {
  suspend fun initialize()
}

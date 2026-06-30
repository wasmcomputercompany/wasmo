package com.wasmo.sql

import okio.ByteString

data class AppDatabaseSecret(
  val value: ByteString,
)

package com.wasmo.accounts

import okio.ByteString

data class CookieSecret(
  val value: ByteString,
)

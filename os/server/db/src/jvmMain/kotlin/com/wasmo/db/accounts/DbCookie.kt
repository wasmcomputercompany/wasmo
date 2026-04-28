package com.wasmo.db.accounts

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.CookieId
import kotlin.time.Instant

data class DbCookie(
  val id: CookieId,
  val createdAt: Instant,
  val accountId: AccountId,
  val token: String,
  val createdByUserAgent: String?,
  val createdByIp: String?,
)

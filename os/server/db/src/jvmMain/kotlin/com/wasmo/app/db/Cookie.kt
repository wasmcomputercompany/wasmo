package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.CookieId
import kotlin.time.Instant

data class Cookie(
  val id: CookieId,
  val created_at: Instant,
  val account_id: AccountId,
  val token: String,
  val created_by_user_agent: String?,
  val created_by_ip: String?,
)

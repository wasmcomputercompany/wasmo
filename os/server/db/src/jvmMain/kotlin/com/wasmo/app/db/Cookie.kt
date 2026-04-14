package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.CookieId
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class Cookie(
  val id: CookieId,
  val created_at: Instant,
  val account_id: AccountId,
  val token: String,
  val created_by_user_agent: String?,
  val created_by_ip: String?,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<CookieId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val account_idAdapter: ColumnAdapter<AccountId, Long>,
  )
}

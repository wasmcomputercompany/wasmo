package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class Invite(
  val id: InviteId,
  val created_at: Instant,
  val created_by: AccountId,
  val version: Int,
  val code: String,
  val claimed_at: Instant?,
  val claimed_by: AccountId?,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<InviteId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val created_byAdapter: ColumnAdapter<AccountId, Long>,
    val claimed_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val claimed_byAdapter: ColumnAdapter<AccountId, Long>,
  )
}

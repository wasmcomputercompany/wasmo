package com.wasmo.db.accounts.invite

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import kotlin.time.Instant

data class Invite(
  val id: InviteId,
  val created_at: Instant,
  val created_by: AccountId,
  val version: Int,
  val code: String,
  val claimed_at: Instant?,
  val claimed_by: AccountId?,
)

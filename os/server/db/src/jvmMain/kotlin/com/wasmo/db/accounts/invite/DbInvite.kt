package com.wasmo.db.accounts.invite

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import kotlin.time.Instant

data class DbInvite(
  val id: InviteId,
  val createdAt: Instant,
  val createdBy: AccountId,
  val version: Int,
  val code: String,
  val claimedAt: Instant?,
  val claimedBy: AccountId?,
)

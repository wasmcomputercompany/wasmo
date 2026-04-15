package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import kotlin.time.Instant

data class ComputerAccess(
  val id: ComputerAccessId,
  val created_at: Instant,
  val version: Int,
  val computer_id: ComputerId,
  val account_id: AccountId,
)

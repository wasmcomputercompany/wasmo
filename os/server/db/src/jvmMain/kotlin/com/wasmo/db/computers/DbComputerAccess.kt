package com.wasmo.db.computers

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.UserId
import kotlin.time.Instant

data class DbComputerAccess(
  val id: ComputerAccessId,
  val createdAt: Instant,
  val version: Long,
  val computerId: ComputerId,
  val accountId: AccountId,
  val userId: UserId,
)

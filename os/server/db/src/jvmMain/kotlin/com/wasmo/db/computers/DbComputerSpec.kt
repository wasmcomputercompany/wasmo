package com.wasmo.db.computers

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import kotlin.time.Instant

data class DbComputerSpec(
  val id: ComputerSpecId,
  val createdAt: Instant,
  val version: Long,
  val accountId: AccountId,
  val token: String,
  val slug: ComputerSlug,
  val computerId: ComputerId?,
)

package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import kotlin.time.Instant

data class ComputerSpec(
  val id: ComputerSpecId,
  val created_at: Instant,
  val version: Long,
  val account_id: AccountId,
  val token: String,
  val slug: ComputerSlug,
  val computer_id: ComputerId?,
)

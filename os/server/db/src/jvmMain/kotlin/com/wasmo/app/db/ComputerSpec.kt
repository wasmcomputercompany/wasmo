package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class ComputerSpec(
  val id: ComputerSpecId,
  val created_at: Instant,
  val version: Long,
  val account_id: AccountId,
  val token: String,
  val slug: ComputerSlug,
  val computer_id: ComputerId?,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<ComputerSpecId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val account_idAdapter: ColumnAdapter<AccountId, Long>,
    val slugAdapter: ColumnAdapter<ComputerSlug, String>,
    val computer_idAdapter: ColumnAdapter<ComputerId, Long>,
  )
}

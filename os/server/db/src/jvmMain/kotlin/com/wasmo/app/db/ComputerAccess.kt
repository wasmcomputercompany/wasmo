package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Long
import kotlin.time.Instant

data class ComputerAccess(
  val id: ComputerAccessId,
  val created_at: Instant,
  val version: Int,
  val computer_id: ComputerId,
  val account_id: AccountId,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<ComputerAccessId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val computer_idAdapter: ColumnAdapter<ComputerId, Long>,
    val account_idAdapter: ColumnAdapter<AccountId, Long>,
  )
}

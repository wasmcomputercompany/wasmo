package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AccountId
import kotlin.Int
import kotlin.Long

data class Account(
  val id: AccountId,
  val version: Int,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<AccountId, Long>,
  )
}

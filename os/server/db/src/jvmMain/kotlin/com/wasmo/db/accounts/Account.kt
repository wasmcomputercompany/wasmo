package com.wasmo.db.accounts

import com.wasmo.identifiers.AccountId

data class Account(
  val id: AccountId,
  val version: Int,
)

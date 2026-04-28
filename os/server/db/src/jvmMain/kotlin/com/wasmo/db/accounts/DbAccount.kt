package com.wasmo.db.accounts

import com.wasmo.identifiers.AccountId

data class DbAccount(
  val id: AccountId,
  val version: Int,
)

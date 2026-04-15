package com.wasmo.app.db

import com.wasmo.identifiers.AccountId

data class Account(
  val id: AccountId,
  val version: Int,
)

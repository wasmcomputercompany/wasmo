package com.wasmo.db.emails

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.LinkedEmailAddressId
import kotlin.time.Instant

data class LinkedEmailAddress(
  val id: LinkedEmailAddressId,
  val createdAt: Instant,
  val accountId: AccountId,
  val emailAddress: String,
  val active: Boolean,
)

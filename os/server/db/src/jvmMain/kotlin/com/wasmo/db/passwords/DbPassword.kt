package com.wasmo.db.passwords

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasswordId
import kotlin.time.Instant

/** authn for local accounts (homelab): username is required, password is optional */
class DbPassword(
  val id: PasswordId,
  val createdAt: Instant,
  val accountId: AccountId,
  val username: String,
  val passwordHash: String?,
  val createdByUserAgent: String?,
  val createdByIp: String?,
  val active: Boolean,
)

package com.wasmo.db.usernames

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.UsernameId
import com.wasmo.identifiers.UsernameSlug
import kotlin.time.Instant

data class DbUsername(
  val id: UsernameId,
  val createdAt: Instant,
  val accountId: AccountId,
  val usernameSlug: UsernameSlug,
)

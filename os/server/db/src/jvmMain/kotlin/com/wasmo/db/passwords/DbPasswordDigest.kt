package com.wasmo.db.passwords

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasswordDigest
import com.wasmo.identifiers.PasswordDigestId
import kotlin.time.Instant

data class DbPasswordDigest(
  val id: PasswordDigestId,
  val createdAt: Instant,
  val accountId: AccountId,
  val passwordDigest: PasswordDigest, // Argon2PasswordHasher produces passwordHashes that are salted and peppered
)

package com.wasmo.db.passkeys

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.passkeys.RegistrationRecord
import kotlin.time.Instant

data class DbPasskey(
  val id: PasskeyId,
  val createdAt: Instant,
  val accountId: AccountId,
  val passkeyId: String,
  val aaguid: String,
  val createdByUserAgent: String?,
  val createdByIp: String?,
  val registrationRecord: RegistrationRecord,
)

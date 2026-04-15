package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.passkeys.RegistrationRecord
import kotlin.time.Instant

data class Passkey(
  val id: PasskeyId,
  val created_at: Instant,
  val account_id: AccountId,
  val passkey_id: String,
  val aaguid: String,
  val created_by_user_agent: String?,
  val created_by_ip: String?,
  val registration_record: RegistrationRecord,
)

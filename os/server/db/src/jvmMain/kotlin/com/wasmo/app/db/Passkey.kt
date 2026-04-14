package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasskeyId
import com.wasmo.passkeys.RegistrationRecord
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String
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
) {
  class Adapter(
    val idAdapter: ColumnAdapter<PasskeyId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val account_idAdapter: ColumnAdapter<AccountId, Long>,
    val registration_recordAdapter: ColumnAdapter<RegistrationRecord, String>,
  )
}

package com.wasmo.db.passkeys

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindJson
import com.wasmo.db.decodeJson
import com.wasmo.db.getAccountId
import com.wasmo.db.getPasskeyId
import com.wasmo.identifiers.AccountId
import com.wasmo.passkeys.RegistrationRecord
import com.wasmo.sql.list
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

context(connection: SqlConnection)
suspend fun findPasskeyByPasskeyId(passkey_id: String): Passkey? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      passkey_id,
      aaguid,
      created_by_user_agent,
      created_by_ip,
      registration_record
    FROM Passkey
    WHERE passkey_id = $1
    """,
  ) {
    bindString(0, passkey_id)
  }

  return rowIterator.singleOrNull {
    getPasskey()
  }
}

context(connection: SqlConnection)
suspend fun findPasskeyByPasskeyIdAndAccountId(
  passkey_id: String,
  account_id: AccountId,
): Passkey? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      passkey_id,
      aaguid,
      created_by_user_agent,
      created_by_ip,
      registration_record
    FROM Passkey
    WHERE passkey_id = $1
      AND account_id = $2
    """,
  ) {
    bindString(0, passkey_id)
    bindAccountId(1, account_id)
  }

  return rowIterator.singleOrNull {
    Passkey(
      getPasskeyId(0),
      getInstant(1)!!,
      getAccountId(2),
      getString(3)!!,
      getString(4)!!,
      getString(5),
      getString(6),
      decodeJson<RegistrationRecord>(7),
    )
  }
}

context(connection: SqlConnection)
suspend fun findPasskeysByAccountId(account_id: AccountId): List<Passkey> {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      passkey_id,
      aaguid,
      created_by_user_agent,
      created_by_ip,
      registration_record
    FROM Passkey
    WHERE account_id = $1
    """,
  ) {
    bindAccountId(0, account_id)
  }
  return rowIterator.list {
    getPasskey()
  }
}

context(connection: SqlConnection)
suspend fun insertPasskey(
  created_at: Instant,
  account_id: AccountId,
  passkey_id: String,
  aaguid: String,
  created_by_user_agent: String?,
  created_by_ip: String?,
  registration_record: RegistrationRecord,
): Long {
  return connection.execute(
    """
    INSERT INTO Passkey(
      created_at,
      account_id,
      passkey_id,
      aaguid,
      created_by_user_agent,
      created_by_ip,
      registration_record
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7
    )
    """,
  ) {
    bindInstant(0, created_at)
    bindAccountId(1, account_id)
    bindString(2, passkey_id)
    bindString(3, aaguid)
    bindString(4, created_by_user_agent)
    bindString(5, created_by_ip)
    bindJson<RegistrationRecord>(6, registration_record)
  }
}

private fun SqlRow.getPasskey(): Passkey = Passkey(
  getPasskeyId(0),
  getInstant(1)!!,
  getAccountId(2),
  getString(3)!!,
  getString(4)!!,
  getString(5),
  getString(6),
  decodeJson<RegistrationRecord>(7),
)

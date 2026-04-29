package com.wasmo.db.passkeys

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindJson
import com.wasmo.db.decodeJson
import com.wasmo.db.getAccountId
import com.wasmo.db.getPasskeyId
import com.wasmo.identifiers.AccountId
import com.wasmo.passkeys.RegistrationRecord
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.list
import wasmox.sql.singleOrNull

context(connection: SqlConnection)
suspend fun findPasskeyByPasskeyId(passkey_id: String): DbPasskey? {
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
  passkeyId: String,
  accountId: AccountId,
): DbPasskey? {
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
    bindString(0, passkeyId)
    bindAccountId(1, accountId)
  }

  return rowIterator.singleOrNull {
    DbPasskey(
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
suspend fun findPasskeysByAccountId(
  accountId: AccountId,
): List<DbPasskey> {
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
    bindAccountId(0, accountId)
  }
  return rowIterator.list {
    getPasskey()
  }
}

context(connection: SqlConnection)
suspend fun insertPasskey(
  createdAt: Instant,
  accountId: AccountId,
  passkeyId: String,
  aaguid: String,
  createdByUserAgent: String?,
  createdByIp: String?,
  registrationRecord: RegistrationRecord,
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
    bindInstant(0, createdAt)
    bindAccountId(1, accountId)
    bindString(2, passkeyId)
    bindString(3, aaguid)
    bindString(4, createdByUserAgent)
    bindString(5, createdByIp)
    bindJson<RegistrationRecord>(6, registrationRecord)
  }
}

private fun SqlRow.getPasskey(): DbPasskey = DbPasskey(
  getPasskeyId(0),
  getInstant(1)!!,
  getAccountId(2),
  getString(3)!!,
  getString(4)!!,
  getString(5),
  getString(6),
  decodeJson<RegistrationRecord>(7),
)

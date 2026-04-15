package com.wasmo.app.db

import com.wasmo.app.db.WasmoDbConnection as SqlDriver
import com.wasmo.identifiers.AccountId
import com.wasmo.passkeys.RegistrationRecord
import kotlin.time.Instant

class PasskeyQueries(
  private val driver: SqlDriver,
) {
  suspend fun findPasskeyByPasskeyId(passkey_id: String): Passkey? {
    val rowIterator = driver.executeQuery(
      """
      SELECT Passkey.id, Passkey.created_at, Passkey.account_id, Passkey.passkey_id, Passkey.aaguid, Passkey.created_by_user_agent, Passkey.created_by_ip, Passkey.registration_record
      FROM Passkey
      WHERE
        passkey_id = $1
      """,
    ) {
      var parameterIndex = 0
      bindString(parameterIndex++, passkey_id)
    }

    return rowIterator.singleOrNull { cursor ->
      Passkey(
        cursor.getPasskeyId(0),
        cursor.getInstant(1)!!,
        cursor.getAccountId(2),
        cursor.getString(3)!!,
        cursor.getString(4)!!,
        cursor.getString(5),
        cursor.getString(6),
        cursor.getJson2<RegistrationRecord>(7),
      )
    }
  }

  suspend fun findPasskeyByPasskeyIdAndAccountId(
    passkey_id: String,
    account_id: AccountId,
  ): Passkey? {
    val rowIterator = driver.executeQuery(
      """
      SELECT Passkey.id, Passkey.created_at, Passkey.account_id, Passkey.passkey_id, Passkey.aaguid, Passkey.created_by_user_agent, Passkey.created_by_ip, Passkey.registration_record FROM Passkey
      WHERE
         passkey_id = $1 AND
         account_id = $2
      """,
    ) {
      var parameterIndex = 0
      bindString(parameterIndex++, passkey_id)
      bindAccountId(parameterIndex++, account_id)
    }

    return rowIterator.singleOrNull { cursor ->
      Passkey(
        cursor.getPasskeyId(0),
        cursor.getInstant(1)!!,
        cursor.getAccountId(2),
        cursor.getString(3)!!,
        cursor.getString(4)!!,
        cursor.getString(5),
        cursor.getString(6),
        cursor.getJson2<RegistrationRecord>(7),
      )
    }
  }

  suspend fun findPasskeysByAccountId(account_id: AccountId): List<Passkey> {
    val rowIterator = driver.executeQuery(
      """SELECT Passkey.id, Passkey.created_at, Passkey.account_id, Passkey.passkey_id, Passkey.aaguid, Passkey.created_by_user_agent, Passkey.created_by_ip, Passkey.registration_record FROM Passkey WHERE account_id = $1""",
    ) {
      var parameterIndex = 0
      bindAccountId(parameterIndex++, account_id)
    }
    return rowIterator.list { cursor ->
      Passkey(
        cursor.getPasskeyId(0),
        cursor.getInstant(1)!!,
        cursor.getAccountId(2),
        cursor.getString(3)!!,
        cursor.getString(4)!!,
        cursor.getString(5),
        cursor.getString(6),
        cursor.getJson2<RegistrationRecord>(7),
      )
    }
  }

  suspend fun insertPasskey(
    created_at: Instant,
    account_id: AccountId,
    passkey_id: String,
    aaguid: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
    registration_record: RegistrationRecord,
  ): Long {
    return driver.execute(
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
      var parameterIndex = 0
      bindInstant(parameterIndex++, created_at)
      bindAccountId(parameterIndex++, account_id)
      bindString(parameterIndex++, passkey_id)
      bindString(parameterIndex++, aaguid)
      bindString(parameterIndex++, created_by_user_agent)
      bindString(parameterIndex++, created_by_ip)
      bindJson<RegistrationRecord>(parameterIndex++, registration_record)
    }
  }
}

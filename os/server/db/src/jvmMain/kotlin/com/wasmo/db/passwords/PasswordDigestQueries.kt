package com.wasmo.db.passwords

import com.wasmo.db.bindAccountId
import com.wasmo.db.getAccountId
import com.wasmo.db.getPasswordDigestId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.PasswordDigest
import com.wasmo.identifiers.PasswordDigestId
import kotlin.time.Clock
import wasmo.sql.SqlConnection
import wasmox.sql.single
import wasmox.sql.singleOrNull

context(connection: SqlConnection)
suspend fun findPasswordDigestForAccount(accountId: AccountId): DbPasswordDigest? {
  val rowIterator = connection.executeQuery(
    """
      SELECT
        id,
        account_id,
        created_at,
        password_digest
      FROM PasswordDigest
      WHERE account_id = $1
    """.trimIndent()
  ) {
    bindAccountId(0, accountId)
  }
  return rowIterator.singleOrNull {
    DbPasswordDigest(
      id = getPasswordDigestId(0),
      accountId = getAccountId(1),
      createdAt = getInstant(2)!!,
      passwordDigest = PasswordDigest(getString(3)!!)
    )
  }
}

context(connection: SqlConnection)
suspend fun removePasswordDigest(accountId: AccountId) {
  connection.execute("DELETE FROM Password WHERE account_id = $1") {
    bindAccountId(0, accountId)
  }
}

context(connection: SqlConnection)
suspend fun insertPasswordDigest(clock: Clock, accountId: AccountId, passwordDigest: PasswordDigest): PasswordDigestId {
  val now = clock.now()
  val rowIterator = connection.executeQuery("""
    INSERT INTO PasswordDigest (
        account_id,
        created_at,
        password_digest
    ) VALUES (
        $1,
        $2,
        $3
    )
    ON CONFLICT (account_id) DO UPDATE SET
        created_at = EXCLUDED.created_at,
        password_digest = EXCLUDED.password_digest
    RETURNING id
    """.trimIndent())
  {
    bindAccountId(0, accountId)
    bindInstant(1, now)
    bindString(2, passwordDigest.value)
  }
  return rowIterator.single {
    getPasswordDigestId(0)
  }
}


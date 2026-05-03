package com.wasmo.db.passwords

import com.wasmo.db.bindAccountId
import com.wasmo.db.getAccountId
import com.wasmo.db.getPasswordId
import com.wasmo.identifiers.AccountId
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.list
import wasmox.sql.singleOrNull

context(connection: SqlConnection)
suspend fun insertPassword(
  createdAt: Instant,
  accountId: AccountId,
  username: String,
  passwordHash: String?,
  createdByUserAgent: String?,
  createdByIp: String?,
  active: Boolean,
): Long {
  return connection.execute(
    """
    INSERT INTO Password(
        created_at,
        account_id,
        username,
        password_hash,
        created_by_user_agent,
        created_by_ip,
        active
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7
    ) RETURNING id
    """,
  ) {
    bindInstant(0, createdAt)
    bindAccountId(1, accountId)
    bindString(2, username)
    bindString(3, passwordHash)
    bindString(4, createdByUserAgent)
    bindString(5, createdByIp)
    bindBool(6, active)
  }
}

context(connection: SqlConnection)
suspend fun findPasswordByPasswordId(passwordId: String): DbPassword? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      username,
      password_hash,
      created_by_user_agent,
      created_by_ip,
      active
    FROM Password
    WHERE id = $1
    """,
  ) {
    bindString(0, passwordId)
  }

  return rowIterator.singleOrNull {
    getPassword()
  }
}

// As long as account_id is UNIQUE, this will at most return 1 result, but we don't hard-code that assumption here.
context(connection: SqlConnection)
suspend fun findBlankPasswordsForAccount(accountId: AccountId, limit: Int = Int.MAX_VALUE): List<DbPassword> =
  connection.executeQuery(
    """
      SELECT
        *
      FROM Password
      WHERE account_id = $1 AND password_hash IS NULL
      LIMIT $2
    """
  ) {
    bindAccountId(0, accountId)
    bindS32(1, limit)
  }.list {
    getPassword()
  }

private fun SqlRow.getPassword(): DbPassword = DbPassword(
  id = getPasswordId(0),
  createdAt = getInstant(1)!!,
  accountId = getAccountId(2),
  username = getString(3)!!,
  passwordHash = getString(4), /* nullable! */
  createdByUserAgent = getString(5),
  createdByIp = getString(6),
  active = getBool(7) ?: false,
)

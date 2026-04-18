package com.wasmo.db.accounts

import com.wasmo.db.bindAccountId
import com.wasmo.db.getAccountId
import com.wasmo.db.getCookieId
import com.wasmo.identifiers.AccountId
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun findCookieByToken(token: String): Cookie? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      token,
      created_by_user_agent,
      created_by_ip
    FROM Cookie
    WHERE token = $1
    """,
  ) {
    bindString(0, token)
  }

  return rowIterator.singleOrNull {
    Cookie(
      getCookieId(0),
      getInstant(1)!!,
      getAccountId(2),
      getString(3)!!,
      getString(4),
      getString(5),
    )
  }
}

context(connection: SqlConnection)
suspend fun insertCookie(
  created_at: Instant,
  account_id: AccountId,
  token: String,
  created_by_user_agent: String?,
  created_by_ip: String?,
): Long {
  return connection.execute(
    """
    INSERT INTO Cookie(
      created_at,
      account_id,
      token,
      created_by_user_agent,
      created_by_ip
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5
    )
    """,
  ) {
    bindInstant(0, created_at)
    bindAccountId(1, account_id)
    bindString(2, token)
    bindString(3, created_by_user_agent)
    bindString(4, created_by_ip)
  }
}

context(connection: SqlConnection)
suspend fun updateAccountIdByAccountId(
  target_account_id: AccountId,
  source_account_id: AccountId,
): Long {
  return connection.execute(
    """
    UPDATE Cookie
    SET account_id = $1
    WHERE account_id = $2
    """,
  ) {
    bindAccountId(0, target_account_id)
    bindAccountId(1, source_account_id)
  }
}

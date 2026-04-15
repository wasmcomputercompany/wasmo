package com.wasmo.app.db

import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.bindAccountId
import com.wasmo.app.db2.getAccountId
import com.wasmo.app.db2.getCookieId
import com.wasmo.app.db2.singleOrNull
import com.wasmo.identifiers.AccountId
import kotlin.time.Instant

class CookieQueries(
  private val driver: SqlDriver,
) {
  suspend fun findCookieByToken(token: String): Cookie? {
    val rowIterator = driver.executeQuery(
      """SELECT Cookie.id, Cookie.created_at, Cookie.account_id, Cookie.token, Cookie.created_by_user_agent, Cookie.created_by_ip FROM Cookie WHERE token = $1""",
    ) {
      var parameterIndex = 0
      bindString(parameterIndex++, token)
    }

    return rowIterator.singleOrNull { cursor ->
      Cookie(
        cursor.getCookieId(0),
        cursor.getInstant(1)!!,
        cursor.getAccountId(2),
        cursor.getString(3)!!,
        cursor.getString(4),
        cursor.getString(5),
      )
    }
  }

  suspend fun insertCookie(
    created_at: Instant,
    account_id: AccountId,
    token: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
  ): Long {
    return driver.execute(
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
      var parameterIndex = 0
      bindInstant(parameterIndex++, created_at)
      bindAccountId(parameterIndex++, account_id)
      bindString(parameterIndex++, token)
      bindString(parameterIndex++, created_by_user_agent)
      bindString(parameterIndex++, created_by_ip)
    }
  }

  suspend fun updateAccountIdByAccountId(
    target_account_id: AccountId,
    source_account_id: AccountId,
  ): Long {
    return driver.execute(
      """
      UPDATE Cookie
      SET account_id = $1
      WHERE account_id = $2
      """,
    ) {
      var parameterIndex = 0
      bindAccountId(parameterIndex++, target_account_id)
      bindAccountId(parameterIndex++, source_account_id)
    }
  }
}

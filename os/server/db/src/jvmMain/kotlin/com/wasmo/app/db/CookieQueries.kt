package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor
import com.wasmo.app.db2.RealSqlCursor as JdbcCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.CookieId
import kotlin.time.Instant

public class CookieQueries(
  private val driver: SqlDriver,
  private val CookieAdapter: Cookie.Adapter,
) {
  public fun <T : Any> findCookieByToken(
    token: String,
    mapper: (
      id: CookieId,
      created_at: Instant,
      account_id: AccountId,
      token: String,
      created_by_user_agent: String?,
      created_by_ip: String?,
    ) -> T,
  ): Query<T> = FindCookieByTokenQuery(token) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      CookieAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      CookieAdapter.account_idAdapter.decode(cursor.getS64(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
    )
  }

  public fun findCookieByToken(token: String): Query<Cookie> = findCookieByToken(token, ::Cookie)

  public fun <T : Any> findCookieByAccountId(
    account_id: AccountId,
    mapper: (
      id: CookieId,
      created_at: Instant,
      account_id: AccountId,
      token: String,
      created_by_user_agent: String?,
      created_by_ip: String?,
    ) -> T,
  ): Query<T> = FindCookieByAccountIdQuery(account_id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      CookieAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      CookieAdapter.account_idAdapter.decode(cursor.getS64(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
    )
  }

  public fun findCookieByAccountId(account_id: AccountId): Query<Cookie> =
    findCookieByAccountId(account_id, ::Cookie)

  /**
   * @return The number of rows updated.
   */
  public suspend fun insertCookie(
    created_at: Instant,
    account_id: AccountId,
    token: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
  ): Long {
    val result = driver.execute(
      """
          |INSERT INTO Cookie(
          |  created_at,
          |  account_id,
          |  token,
          |  created_by_user_agent,
          |  created_by_ip
          |)
          |VALUES (
          |  $1,
          |  $2,
          |  $3,
          |  $4,
          |  $5
          |)
          """.trimMargin(),
    ) {
      var parameterIndex = 0
      bindInstant(parameterIndex++, created_at)
      bindS64(parameterIndex++, CookieAdapter.account_idAdapter.encode(account_id))
      bindString(parameterIndex++, token)
      bindString(parameterIndex++, created_by_user_agent)
      bindString(parameterIndex++, created_by_ip)
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public suspend fun updateAccountIdByAccountId(
    target_account_id: AccountId,
    source_account_id: AccountId,
  ): Long {
    val result = driver.execute(
      """
          |UPDATE Cookie
          |SET account_id = $1
          |WHERE account_id = $2
          """.trimMargin(),
    ) {
      var parameterIndex = 0
      bindS64(parameterIndex++, CookieAdapter.account_idAdapter.encode(target_account_id))
      bindS64(parameterIndex++, CookieAdapter.account_idAdapter.encode(source_account_id))
    }
    return result
  }

  private inner class FindCookieByTokenQuery<out T : Any>(
    public val token: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """SELECT Cookie.id, Cookie.created_at, Cookie.account_id, Cookie.token, Cookie.created_by_user_agent, Cookie.created_by_ip FROM Cookie WHERE token = $1""",
      ) {
        var parameterIndex = 0
        bindString(parameterIndex++, token)
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "Cookie.sq:findCookieByToken"
  }

  private inner class FindCookieByAccountIdQuery<out T : Any>(
    public val account_id: AccountId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """SELECT Cookie.id, Cookie.created_at, Cookie.account_id, Cookie.token, Cookie.created_by_user_agent, Cookie.created_by_ip FROM Cookie WHERE account_id = $1""",
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, CookieAdapter.account_idAdapter.encode(account_id))
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "Cookie.sq:findCookieByAccountId"
  }
}

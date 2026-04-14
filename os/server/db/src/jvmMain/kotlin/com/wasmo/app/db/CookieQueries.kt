package com.wasmo.app.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.CookieId
import java.time.OffsetDateTime
import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

public class CookieQueries(
  driver: SqlDriver,
  private val CookieAdapter: Cookie.Adapter,
) : TransacterImpl(driver) {
  public fun <T : Any> findCookieByToken(token: String, mapper: (
    id: CookieId,
    created_at: Instant,
    account_id: AccountId,
    token: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
  ) -> T): Query<T> = FindCookieByTokenQuery(token) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      CookieAdapter.idAdapter.decode(cursor.getLong(0)!!),
      CookieAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      CookieAdapter.account_idAdapter.decode(cursor.getLong(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5)
    )
  }

  public fun findCookieByToken(token: String): Query<Cookie> = findCookieByToken(token, ::Cookie)

  public fun <T : Any> findCookieByAccountId(account_id: AccountId, mapper: (
    id: CookieId,
    created_at: Instant,
    account_id: AccountId,
    token: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
  ) -> T): Query<T> = FindCookieByAccountIdQuery(account_id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      CookieAdapter.idAdapter.decode(cursor.getLong(0)!!),
      CookieAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      CookieAdapter.account_idAdapter.decode(cursor.getLong(2)!!),
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5)
    )
  }

  public fun findCookieByAccountId(account_id: AccountId): Query<Cookie> = findCookieByAccountId(account_id, ::Cookie)

  /**
   * @return The number of rows updated.
   */
  public fun insertCookie(
    created_at: Instant,
    account_id: AccountId,
    token: String,
    created_by_user_agent: String?,
    created_by_ip: String?,
  ): QueryResult<Long> {
    val result = driver.execute(-1_227_629_861, """
        |INSERT INTO Cookie(
        |  created_at,
        |  account_id,
        |  token,
        |  created_by_user_agent,
        |  created_by_ip
        |)
        |VALUES (
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?
        |)
        """.trimMargin(), 5) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindObject(parameterIndex++, CookieAdapter.created_atAdapter.encode(created_at))
          bindLong(parameterIndex++, CookieAdapter.account_idAdapter.encode(account_id))
          bindString(parameterIndex++, token)
          bindString(parameterIndex++, created_by_user_agent)
          bindString(parameterIndex++, created_by_ip)
        }
    notifyQueries(-1_227_629_861) { emit ->
      emit("Cookie")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateAccountIdByAccountId(target_account_id: AccountId, source_account_id: AccountId): QueryResult<Long> {
    val result = driver.execute(-1_742_666_096, """
        |UPDATE Cookie
        |SET account_id = ?
        |WHERE account_id = ?
        """.trimMargin(), 2) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindLong(parameterIndex++, CookieAdapter.account_idAdapter.encode(target_account_id))
          bindLong(parameterIndex++, CookieAdapter.account_idAdapter.encode(source_account_id))
        }
    notifyQueries(-1_742_666_096) { emit ->
      emit("Cookie")
    }
    return result
  }

  private inner class FindCookieByTokenQuery<out T : Any>(
    public val token: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("Cookie", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("Cookie", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(316_570_119, """SELECT Cookie.id, Cookie.created_at, Cookie.account_id, Cookie.token, Cookie.created_by_user_agent, Cookie.created_by_ip FROM Cookie WHERE token = ?""", mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, token)
    }

    override fun toString(): String = "Cookie.sq:findCookieByToken"
  }

  private inner class FindCookieByAccountIdQuery<out T : Any>(
    public val account_id: AccountId,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("Cookie", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("Cookie", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_236_875_978, """SELECT Cookie.id, Cookie.created_at, Cookie.account_id, Cookie.token, Cookie.created_by_user_agent, Cookie.created_by_ip FROM Cookie WHERE account_id = ?""", mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, CookieAdapter.account_idAdapter.encode(account_id))
    }

    override fun toString(): String = "Cookie.sq:findCookieByAccountId"
  }
}

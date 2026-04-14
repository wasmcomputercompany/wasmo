package com.wasmo.app.db

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId

public class AccountQueries(
  private val driver: SqlDriver,
  private val AccountAdapter: Account.Adapter,
) {
  public fun insertAccount(version: Int): ExecutableQuery<AccountId> = InsertAccountQuery(version) { cursor ->
    check(cursor is JdbcCursor)
    AccountAdapter.idAdapter.decode(cursor.getLong(0)!!)
  }

  public fun <T : Any> getById(id: AccountId, mapper: (id: AccountId, version: Int) -> T): Query<T> = GetByIdQuery(id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      AccountAdapter.idAdapter.decode(cursor.getLong(0)!!),
      cursor.getInt(1)!!
    )
  }

  public fun getById(id: AccountId): Query<Account> = getById(id, ::Account)

  private inner class InsertAccountQuery<out T : Any>(
    public val version: Int,
    mapper: (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-2_040_971_259, """
    |INSERT INTO Account(
    |  version
    |)
    |VALUES (
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindInt(parameterIndex++, version)
    }

    override fun toString(): String = "Account.sq:insertAccount"
  }

  private inner class GetByIdQuery<out T : Any>(
    public val id: AccountId,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_074_845_735, """SELECT Account.id, Account.version FROM Account WHERE id = ?""", mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, AccountAdapter.idAdapter.encode(id))
    }

    override fun toString(): String = "Account.sq:getById"
  }
}

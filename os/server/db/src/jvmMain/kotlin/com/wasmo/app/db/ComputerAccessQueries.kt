package com.wasmo.app.db

import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import kotlin.time.Instant

public class ComputerAccessQueries(
  private val driver: SqlDriver,
  private val ComputerAccessAdapter: ComputerAccess.Adapter,
) {
  public fun insertComputerAccess(
    created_at: Instant,
    version: Int,
    computer_id: ComputerId,
    account_id: AccountId,
  ): ExecutableQuery<ComputerAccessId> = InsertComputerAccessQuery(created_at, version, computer_id, account_id) { cursor ->
    check(cursor is JdbcCursor)
    ComputerAccessAdapter.idAdapter.decode(cursor.getLong(0)!!)
  }

  private inner class InsertComputerAccessQuery<out T : Any>(
    public val created_at: Instant,
    public val version: Int,
    public val computer_id: ComputerId,
    public val account_id: AccountId,
    mapper: (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun <R> execute(mapper: (SqlCursor) -> R): R = driver.executeQuery(-605_613_359, """
    |INSERT INTO ComputerAccess(
    |  created_at,
    |  version,
    |  computer_id,
    |  account_id
    |)
    |VALUES (
    |  ?,
    |  ?,
    |  ?,
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 4) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindObject(parameterIndex++, ComputerAccessAdapter.created_atAdapter.encode(created_at))
      bindInt(parameterIndex++, version)
      bindLong(parameterIndex++, ComputerAccessAdapter.computer_idAdapter.encode(computer_id))
      bindLong(parameterIndex++, ComputerAccessAdapter.account_idAdapter.encode(account_id))
    }

    override fun toString(): String = "ComputerAccess.sq:insertComputerAccess"
  }
}

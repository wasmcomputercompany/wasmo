package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import wasmo.sql.RowIterator

class AccountQueries(
  private val driver: SqlDriver,
  private val AccountAdapter: Account.Adapter,
) {
  fun insertAccount(version: Int): ExecutableQuery<AccountId> =
    InsertAccountQuery(version) { cursor ->
      AccountAdapter.idAdapter.decode(cursor.getS64(0)!!)
    }

  private inner class InsertAccountQuery<out T : Any>(
    val version: Int,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """
        |INSERT INTO Account(
        |  version
        |)
        |VALUES (
        |  $1
        |) RETURNING id
        """.trimMargin(),
      ) {
        var parameterIndex = 0
        bindS32(parameterIndex++, version)
      }
    }

    override fun toString(): String = "Account.sq:insertAccount"
  }
}

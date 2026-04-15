package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.bindAccountId
import com.wasmo.app.db2.bindComputerId
import com.wasmo.app.db2.getComputerAccessId
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import kotlin.time.Instant
import wasmo.sql.RowIterator

public class ComputerAccessQueries(
  private val driver: SqlDriver,
) {
  public fun insertComputerAccess(
    created_at: Instant,
    version: Int,
    computer_id: ComputerId,
    account_id: AccountId,
  ): ExecutableQuery<ComputerAccessId> =
    InsertComputerAccessQuery(created_at, version, computer_id, account_id) { cursor ->
      cursor.getComputerAccessId(0)
    }

  private inner class InsertComputerAccessQuery<out T : Any>(
    public val created_at: Instant,
    public val version: Int,
    public val computer_id: ComputerId,
    public val account_id: AccountId,
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """
          |INSERT INTO ComputerAccess(
          |  created_at,
          |  version,
          |  computer_id,
          |  account_id
          |)
          |VALUES (
          |  $1,
          |  $2,
          |  $3,
          |  $4
          |) RETURNING id
          """.trimMargin(),
      ) {
        var parameterIndex = 0
        bindInstant(parameterIndex++, created_at)
        bindS32(parameterIndex++, version)
        bindComputerId(parameterIndex++, computer_id)
        bindAccountId(parameterIndex++, account_id)
      }
    }

    override fun toString(): String = "ComputerAccess.sq:insertComputerAccess"
  }
}

package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
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
    ComputerAccessAdapter.idAdapter.decode(cursor.getS64(0)!!)
  }

  private inner class InsertComputerAccessQuery<out T : Any>(
    public val created_at: Instant,
    public val version: Int,
    public val computer_id: ComputerId,
    public val account_id: AccountId,
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
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
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindInstant(parameterIndex++, created_at)
        bindS32(parameterIndex++, version)
        bindS64(parameterIndex++, ComputerAccessAdapter.computer_idAdapter.encode(computer_id))
        bindS64(parameterIndex++, ComputerAccessAdapter.account_idAdapter.encode(account_id))
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "ComputerAccess.sq:insertComputerAccess"
  }
}

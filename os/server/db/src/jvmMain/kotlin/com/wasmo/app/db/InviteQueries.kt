package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.bindAccountId
import com.wasmo.app.db2.bindInviteId
import com.wasmo.app.db2.getAccountId
import com.wasmo.app.db2.getAccountIdOrNull
import com.wasmo.app.db2.getInviteId
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import kotlin.time.Instant
import wasmo.sql.RowIterator

public class InviteQueries(
  private val driver: SqlDriver,
) {
  public fun <T : Any> findInvitesByClaimedBy(
    claimed_by: AccountId?,
    limit: Long,
    mapper: (
      id: InviteId,
      created_at: Instant,
      created_by: AccountId,
      version: Int,
      code: String,
      claimed_at: Instant?,
      claimed_by: AccountId?,
    ) -> T,
  ): Query<T> = FindInvitesByClaimedByQuery(claimed_by, limit) { cursor ->
    mapper(
      cursor.getInviteId(0),
      cursor.getInstant(1)!!,
      cursor.getAccountId(2),
      cursor.getS32(3)!!,
      cursor.getString(4)!!,
      cursor.getInstant(5),
      cursor.getAccountIdOrNull(6),
    )
  }

  public fun findInvitesByClaimedBy(claimed_by: AccountId?, limit: Long): Query<Invite> =
    findInvitesByClaimedBy(claimed_by, limit, ::Invite)

  public fun <T : Any> findInvitesByCode(
    code: String,
    mapper: (
      id: InviteId,
      created_at: Instant,
      created_by: AccountId,
      version: Int,
      code: String,
      claimed_at: Instant?,
      claimed_by: AccountId?,
    ) -> T,
  ): Query<T> = FindInvitesByCodeQuery(code) { cursor ->
    mapper(
      cursor.getInviteId(0),
      cursor.getInstant(1)!!,
      cursor.getAccountId(2),
      cursor.getS32(3)!!,
      cursor.getString(4)!!,
      cursor.getInstant(5),
      cursor.getAccountIdOrNull(6),
    )
  }

  public fun findInvitesByCode(code: String): Query<Invite> = findInvitesByCode(code, ::Invite)

  /**
   * @return The number of rows updated.
   */
  public suspend fun insertInvite(
    created_at: Instant,
    created_by: AccountId,
    version: Int,
    code: String,
  ): Long {
    val result = driver.execute(
      """
          |INSERT INTO Invite(
          |  created_at,
          |  created_by,
          |  version,
          |  code
          |)
          |VALUES (
          |  $1,
          |  $2,
          |  $3,
          |  $4
          |)
          """.trimMargin(),
    ) {
      var parameterIndex = 0
      bindInstant(parameterIndex++, created_at)
      bindAccountId(parameterIndex++, created_by)
      bindS32(parameterIndex++, version)
      bindString(parameterIndex++, code)
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public suspend fun claimInvite(
    new_version: Int,
    claimed_at: Instant?,
    claimed_by: AccountId?,
    expected_version: Int,
    id: InviteId,
  ): Long {
    val result = driver.execute(
      """
          |UPDATE Invite
          |SET
          |  version = $1,
          |  claimed_at = $2,
          |  claimed_by = $3
          |WHERE
          |  version = $4 AND
          |  id = $5
          """.trimMargin(),
    ) {
      var parameterIndex = 0
      bindS32(parameterIndex++, new_version)
      bindInstant(parameterIndex++, claimed_at)
      bindAccountId(parameterIndex++, claimed_by)
      bindS32(parameterIndex++, expected_version)
      bindInviteId(parameterIndex++, id)
    }
    return result
  }

  private inner class FindInvitesByClaimedByQuery<out T : Any>(
    public val claimed_by: AccountId?,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """SELECT Invite.id, Invite.created_at, Invite.created_by, Invite.version, Invite.code, Invite.claimed_at, Invite.claimed_by FROM Invite WHERE claimed_by ${if (claimed_by == null) "IS" else "="} $1 LIMIT $2""",
      ) {
        var parameterIndex = 0
        bindAccountId(parameterIndex++, claimed_by)
        bindS64(parameterIndex++, limit)
      }
    }

    override fun toString(): String = "Invite.sq:findInvitesByClaimedBy"
  }

  private inner class FindInvitesByCodeQuery<out T : Any>(
    public val code: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
        """SELECT Invite.id, Invite.created_at, Invite.created_by, Invite.version, Invite.code, Invite.claimed_at, Invite.claimed_by FROM Invite WHERE code = $1""",
      ) {
        var parameterIndex = 0
        bindString(parameterIndex++, code)
      }
    }

    override fun toString(): String = "Invite.sq:findInvitesByCode"
  }
}

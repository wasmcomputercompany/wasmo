package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import java.time.OffsetDateTime
import kotlin.time.Instant

public class InviteQueries(
  private val driver: SqlDriver,
  private val InviteAdapter: Invite.Adapter,
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
    check(cursor is JdbcCursor)
    mapper(
      InviteAdapter.idAdapter.decode(cursor.getLong(0)!!),
      InviteAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      InviteAdapter.created_byAdapter.decode(cursor.getLong(2)!!),
      cursor.getInt(3)!!,
      cursor.getString(4)!!,
      cursor.getObject<OffsetDateTime>(5)?.let { InviteAdapter.claimed_atAdapter.decode(it) },
      cursor.getLong(6)?.let { InviteAdapter.claimed_byAdapter.decode(it) }
    )
  }

  public fun findInvitesByClaimedBy(claimed_by: AccountId?, limit: Long): Query<Invite> = findInvitesByClaimedBy(claimed_by, limit, ::Invite)

  public fun <T : Any> findInvitesByCode(code: String, mapper: (
    id: InviteId,
    created_at: Instant,
    created_by: AccountId,
    version: Int,
    code: String,
    claimed_at: Instant?,
    claimed_by: AccountId?,
  ) -> T): Query<T> = FindInvitesByCodeQuery(code) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      InviteAdapter.idAdapter.decode(cursor.getLong(0)!!),
      InviteAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      InviteAdapter.created_byAdapter.decode(cursor.getLong(2)!!),
      cursor.getInt(3)!!,
      cursor.getString(4)!!,
      cursor.getObject<OffsetDateTime>(5)?.let { InviteAdapter.claimed_atAdapter.decode(it) },
      cursor.getLong(6)?.let { InviteAdapter.claimed_byAdapter.decode(it) }
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
    val result = driver.execute(132_921_893, """
        |INSERT INTO Invite(
        |  created_at,
        |  created_by,
        |  version,
        |  code
        |)
        |VALUES (
        |  ?,
        |  ?,
        |  ?,
        |  ?
        |)
        """.trimMargin(), 4) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindObject(parameterIndex++, InviteAdapter.created_atAdapter.encode(created_at))
          bindLong(parameterIndex++, InviteAdapter.created_byAdapter.encode(created_by))
          bindInt(parameterIndex++, version)
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
    val result = driver.execute(-1_917_276_414, """
        |UPDATE Invite
        |SET
        |  version = ?,
        |  claimed_at = ?,
        |  claimed_by = ?
        |WHERE
        |  version = ? AND
        |  id = ?
        """.trimMargin(), 5) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindInt(parameterIndex++, new_version)
          bindObject(parameterIndex++, claimed_at?.let { InviteAdapter.claimed_atAdapter.encode(it) })
          bindLong(parameterIndex++, claimed_by?.let { InviteAdapter.claimed_byAdapter.encode(it) })
          bindInt(parameterIndex++, expected_version)
          bindLong(parameterIndex++, InviteAdapter.idAdapter.encode(id))
        }
    return result
  }

  private inner class FindInvitesByClaimedByQuery<out T : Any>(
    public val claimed_by: AccountId?,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(null, """SELECT Invite.id, Invite.created_at, Invite.created_by, Invite.version, Invite.code, Invite.claimed_at, Invite.claimed_by FROM Invite WHERE claimed_by ${ if (claimed_by == null) "IS" else "=" } ? LIMIT ?""", mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, claimed_by?.let { InviteAdapter.claimed_byAdapter.encode(it) })
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "Invite.sq:findInvitesByClaimedBy"
  }

  private inner class FindInvitesByCodeQuery<out T : Any>(
    public val code: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(445_097_906, """SELECT Invite.id, Invite.created_at, Invite.created_by, Invite.version, Invite.code, Invite.claimed_at, Invite.claimed_by FROM Invite WHERE code = ?""", mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, code)
    }

    override fun toString(): String = "Invite.sq:findInvitesByCode"
  }
}

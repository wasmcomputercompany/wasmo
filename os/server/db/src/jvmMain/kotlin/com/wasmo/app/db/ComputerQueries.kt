package com.wasmo.app.db

import app.cash.sqldelight.db.QueryResult
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import java.time.OffsetDateTime
import kotlin.time.Instant

public class ComputerQueries(
  private val driver: SqlDriver,
  private val ComputerAdapter: Computer.Adapter,
  private val ComputerAccessAdapter: ComputerAccess.Adapter,
) {
  public fun insertComputer(
    created_at: Instant,
    version: Long,
    slug: ComputerSlug,
  ): ExecutableQuery<ComputerId> = InsertComputerQuery(created_at, version, slug) { cursor ->
    check(cursor is JdbcCursor)
    ComputerAdapter.idAdapter.decode(cursor.getLong(0)!!)
  }

  public fun <T : Any> selectComputersByAccountId(
    account_id: AccountId,
    limit: Long,
    mapper: (
      id: ComputerId,
      created_at: Instant,
      version: Long,
      slug: ComputerSlug,
    ) -> T,
  ): Query<T> = SelectComputersByAccountIdQuery(account_id, limit) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerAdapter.idAdapter.decode(cursor.getLong(0)!!),
      ComputerAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getLong(2)!!,
      ComputerAdapter.slugAdapter.decode(cursor.getString(3)!!)
    )
  }

  public fun selectComputersByAccountId(account_id: AccountId, limit: Long): Query<Computer> = selectComputersByAccountId(account_id, limit, ::Computer)

  public fun <T : Any> selectComputerByAccountIdAndSlug(
    account_id: AccountId,
    slug: ComputerSlug,
    mapper: (
      id: ComputerId,
      created_at: Instant,
      version: Long,
      slug: ComputerSlug,
    ) -> T,
  ): Query<T> = SelectComputerByAccountIdAndSlugQuery(account_id, slug) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerAdapter.idAdapter.decode(cursor.getLong(0)!!),
      ComputerAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getLong(2)!!,
      ComputerAdapter.slugAdapter.decode(cursor.getString(3)!!)
    )
  }

  public fun selectComputerByAccountIdAndSlug(account_id: AccountId, slug: ComputerSlug): Query<Computer> = selectComputerByAccountIdAndSlug(account_id, slug, ::Computer)

  public fun <T : Any> selectComputerById(id: ComputerId, mapper: (
    id: ComputerId,
    created_at: Instant,
    version: Long,
    slug: ComputerSlug,
  ) -> T): Query<T> = SelectComputerByIdQuery(id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerAdapter.idAdapter.decode(cursor.getLong(0)!!),
      ComputerAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getLong(2)!!,
      ComputerAdapter.slugAdapter.decode(cursor.getString(3)!!)
    )
  }

  public fun selectComputerById(id: ComputerId): Query<Computer> = selectComputerById(id, ::Computer)

  private inner class InsertComputerQuery<out T : Any>(
    public val created_at: Instant,
    public val version: Long,
    public val slug: ComputerSlug,
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-1_964_359_735, """
    |INSERT INTO Computer(
    |  created_at,
    |  version,
    |  slug
    |)
    |VALUES (
    |  ?,
    |  ?,
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 3) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindObject(parameterIndex++, ComputerAdapter.created_atAdapter.encode(created_at))
      bindLong(parameterIndex++, version)
      bindString(parameterIndex++, ComputerAdapter.slugAdapter.encode(slug))
    }

    override fun toString(): String = "Computer.sq:insertComputer"
  }

  private inner class SelectComputersByAccountIdQuery<out T : Any>(
    public val account_id: AccountId,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-11_321_302, """
    |SELECT
    |  c.id, c.created_at, c.version, c.slug
    |FROM
    |  ComputerAccess ca,
    |  Computer c
    |WHERE
    |  c.id = ca.id AND
    |  ca.account_id = ?
    |ORDER BY
    |  c.slug
    |LIMIT ?
    """.trimMargin(), mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, ComputerAccessAdapter.account_idAdapter.encode(account_id))
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "Computer.sq:selectComputersByAccountId"
  }

  private inner class SelectComputerByAccountIdAndSlugQuery<out T : Any>(
    public val account_id: AccountId,
    public val slug: ComputerSlug,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(341_935_741, """
    |SELECT
    |  c.id, c.created_at, c.version, c.slug
    |FROM
    |  ComputerAccess ca,
    |  Computer c
    |WHERE
    |  c.id = ca.id AND
    |  ca.account_id = ? AND
    |  c.slug = ?
    |LIMIT 1
    """.trimMargin(), mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, ComputerAccessAdapter.account_idAdapter.encode(account_id))
      bindString(parameterIndex++, ComputerAdapter.slugAdapter.encode(slug))
    }

    override fun toString(): String = "Computer.sq:selectComputerByAccountIdAndSlug"
  }

  private inner class SelectComputerByIdQuery<out T : Any>(
    public val id: ComputerId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-106_878_722, """
    |SELECT Computer.id, Computer.created_at, Computer.version, Computer.slug
    |FROM
    |  Computer
    |WHERE
    |  id = ?
    |LIMIT 1
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, ComputerAdapter.idAdapter.encode(id))
    }

    override fun toString(): String = "Computer.sq:selectComputerById"
  }
}

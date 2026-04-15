package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor
import com.wasmo.app.db2.RealSqlCursor as JdbcCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
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
    ComputerAdapter.idAdapter.decode(cursor.getS64(0)!!)
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
      ComputerAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS64(2)!!,
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
      ComputerAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS64(2)!!,
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
      ComputerAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS64(2)!!,
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
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """
          |INSERT INTO Computer(
          |  created_at,
          |  version,
          |  slug
          |)
          |VALUES (
          |  $1,
          |  $2,
          |  $3
          |) RETURNING id
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindInstant(parameterIndex++, created_at)
        bindS64(parameterIndex++, version)
        bindString(parameterIndex++, ComputerAdapter.slugAdapter.encode(slug))
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "Computer.sq:insertComputer"
  }

  private inner class SelectComputersByAccountIdQuery<out T : Any>(
    public val account_id: AccountId,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """
          |SELECT
          |  c.id, c.created_at, c.version, c.slug
          |FROM
          |  ComputerAccess ca,
          |  Computer c
          |WHERE
          |  c.id = ca.id AND
          |  ca.account_id = $1
          |ORDER BY
          |  c.slug
          |LIMIT $2
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, ComputerAccessAdapter.account_idAdapter.encode(account_id))
        bindS64(parameterIndex++, limit)
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "Computer.sq:selectComputersByAccountId"
  }

  private inner class SelectComputerByAccountIdAndSlugQuery<out T : Any>(
    public val account_id: AccountId,
    public val slug: ComputerSlug,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """
          |SELECT
          |  c.id, c.created_at, c.version, c.slug
          |FROM
          |  ComputerAccess ca,
          |  Computer c
          |WHERE
          |  c.id = ca.id AND
          |  ca.account_id = $1 AND
          |  c.slug = $2
          |LIMIT 1
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, ComputerAccessAdapter.account_idAdapter.encode(account_id))
        bindString(parameterIndex++, ComputerAdapter.slugAdapter.encode(slug))
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "Computer.sq:selectComputerByAccountIdAndSlug"
  }

  private inner class SelectComputerByIdQuery<out T : Any>(
    public val id: ComputerId,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """
          |SELECT Computer.id, Computer.created_at, Computer.version, Computer.slug
          |FROM
          |  Computer
          |WHERE
          |  id = $1
          |LIMIT 1
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindS64(parameterIndex++, ComputerAdapter.idAdapter.encode(id))
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "Computer.sq:selectComputerById"
  }
}

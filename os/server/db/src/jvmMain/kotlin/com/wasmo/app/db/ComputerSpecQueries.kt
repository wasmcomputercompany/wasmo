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
import com.wasmo.identifiers.ComputerSpecId
import kotlin.time.Instant

public class ComputerSpecQueries(
  private val driver: SqlDriver,
  private val ComputerSpecAdapter: ComputerSpec.Adapter,
) {
  public fun <T : Any> selectComputerSpecByToken(
    token: String,
    mapper: (
      id: ComputerSpecId,
      created_at: Instant,
      version: Long,
      account_id: AccountId,
      token: String,
      slug: ComputerSlug,
      computer_id: ComputerId?,
    ) -> T,
  ): Query<T> = SelectComputerSpecByTokenQuery(token) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerSpecAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS64(2)!!,
      ComputerSpecAdapter.account_idAdapter.decode(cursor.getS64(3)!!),
      cursor.getString(4)!!,
      ComputerSpecAdapter.slugAdapter.decode(cursor.getString(5)!!),
      cursor.getS64(6)?.let { ComputerSpecAdapter.computer_idAdapter.decode(it) },
    )
  }

  public fun selectComputerSpecByToken(token: String): Query<ComputerSpec> =
    selectComputerSpecByToken(token, ::ComputerSpec)

  public fun insertComputerSpec(
    created_at: Instant,
    version: Long,
    account_id: AccountId,
    token: String,
    slug: ComputerSlug,
  ): ExecutableQuery<ComputerSpecId> =
    InsertComputerSpecQuery(created_at, version, account_id, token, slug) { cursor ->
      check(cursor is JdbcCursor)
      ComputerSpecAdapter.idAdapter.decode(cursor.getS64(0)!!)
    }

  /**
   * @return The number of rows updated.
   */
  public suspend fun linkComputer(
    new_version: Long,
    computer_id: ComputerId?,
    expected_version: Long,
    id: ComputerSpecId,
  ): Long {
    val result = driver.execute(
      """
          |UPDATE ComputerSpec
          |SET
          |  version = $1,
          |  computer_id = $2
          |WHERE
          |  version = $3 AND
          |  id = $4
          """.trimMargin(),
    ) {
      var parameterIndex = 0
      bindS64(parameterIndex++, new_version)
      bindS64(
        parameterIndex++,
        computer_id?.let { ComputerSpecAdapter.computer_idAdapter.encode(it) },
      )
      bindS64(parameterIndex++, expected_version)
      bindS64(parameterIndex++, ComputerSpecAdapter.idAdapter.encode(id))
    }
    return result
  }

  private inner class SelectComputerSpecByTokenQuery<out T : Any>(
    public val token: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """
          |SELECT ComputerSpec.id, ComputerSpec.created_at, ComputerSpec.version, ComputerSpec.account_id, ComputerSpec.token, ComputerSpec.slug, ComputerSpec.computer_id
          |FROM
          |  ComputerSpec
          |WHERE
          |  token = $1
          |LIMIT 1
          """.trimMargin(),
      ) {
        var parameterIndex = 0
        bindString(parameterIndex++, token)
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "ComputerSpec.sq:selectComputerSpecByToken"
  }

  private inner class InsertComputerSpecQuery<out T : Any>(
    public val created_at: Instant,
    public val version: Long,
    public val account_id: AccountId,
    public val token: String,
    public val slug: ComputerSlug,
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
      val rowIterator = driver.executeQuery(
        """
          |INSERT INTO ComputerSpec(
          |  created_at,
          |  version,
          |  account_id,
          |  token,
          |  slug
          |)
          |VALUES (
          |  $1,
          |  $2,
          |  $3,
          |  $4,
          |  $5
          |) RETURNING id
          """.trimMargin()
      ) {
        var parameterIndex = 0
        bindInstant(parameterIndex++, created_at)
        bindS64(parameterIndex++, version)
        bindS64(parameterIndex++, ComputerSpecAdapter.account_idAdapter.encode(account_id))
        bindString(parameterIndex++, token)
        bindString(parameterIndex++, ComputerSpecAdapter.slugAdapter.encode(slug))
      }
      return mapper(RealSqlCursor(rowIterator))
    }

    override fun toString(): String = "ComputerSpec.sq:insertComputerSpec"
  }
}

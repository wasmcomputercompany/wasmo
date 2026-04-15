package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.bindAccountId
import com.wasmo.app.db2.bindComputerId
import com.wasmo.app.db2.bindComputerSlug
import com.wasmo.app.db2.bindComputerSpecId
import com.wasmo.app.db2.getAccountId
import com.wasmo.app.db2.getComputerIdOrNull
import com.wasmo.app.db2.getComputerSlug
import com.wasmo.app.db2.getComputerSpecId
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import kotlin.time.Instant
import wasmo.sql.RowIterator

public class ComputerSpecQueries(
  private val driver: SqlDriver,
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
    mapper(
      cursor.getComputerSpecId(0),
      cursor.getInstant(1)!!,
      cursor.getS64(2)!!,
      cursor.getAccountId(3),
      cursor.getString(4)!!,
      cursor.getComputerSlug(5),
      cursor.getComputerIdOrNull(6),
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
      cursor.getComputerSpecId(0)
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
      bindComputerId(parameterIndex++, computer_id)
      bindS64(parameterIndex++, expected_version)
      bindComputerSpecId(parameterIndex++, id)
    }
    return result
  }

  private inner class SelectComputerSpecByTokenQuery<out T : Any>(
    public val token: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
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
    override suspend fun execute(): RowIterator {
      return driver.executeQuery(
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
        bindAccountId(parameterIndex++, account_id)
        bindString(parameterIndex++, token)
        bindComputerSlug(parameterIndex++, slug)
      }
    }

    override fun toString(): String = "ComputerSpec.sq:insertComputerSpec"
  }
}

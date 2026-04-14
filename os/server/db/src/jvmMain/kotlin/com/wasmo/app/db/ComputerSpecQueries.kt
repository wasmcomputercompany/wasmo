package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import java.time.OffsetDateTime
import kotlin.time.Instant

public class ComputerSpecQueries(
  private val driver: SqlDriver,
  private val ComputerSpecAdapter: ComputerSpec.Adapter,
) {
  public fun <T : Any> selectComputerSpecByToken(token: String, mapper: (
    id: ComputerSpecId,
    created_at: Instant,
    version: Long,
    account_id: AccountId,
    token: String,
    slug: ComputerSlug,
    computer_id: ComputerId?,
  ) -> T): Query<T> = SelectComputerSpecByTokenQuery(token) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerSpecAdapter.idAdapter.decode(cursor.getLong(0)!!),
      ComputerSpecAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getLong(2)!!,
      ComputerSpecAdapter.account_idAdapter.decode(cursor.getLong(3)!!),
      cursor.getString(4)!!,
      ComputerSpecAdapter.slugAdapter.decode(cursor.getString(5)!!),
      cursor.getLong(6)?.let { ComputerSpecAdapter.computer_idAdapter.decode(it) }
    )
  }

  public fun selectComputerSpecByToken(token: String): Query<ComputerSpec> = selectComputerSpecByToken(token, ::ComputerSpec)

  public fun insertComputerSpec(
    created_at: Instant,
    version: Long,
    account_id: AccountId,
    token: String,
    slug: ComputerSlug,
  ): ExecutableQuery<ComputerSpecId> = InsertComputerSpecQuery(created_at, version, account_id, token, slug) { cursor ->
    check(cursor is JdbcCursor)
    ComputerSpecAdapter.idAdapter.decode(cursor.getLong(0)!!)
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
    val result = driver.execute(94_427_045, """
        |UPDATE ComputerSpec
        |SET
        |  version = ?,
        |  computer_id = ?
        |WHERE
        |  version = ? AND
        |  id = ?
        """.trimMargin(), 4) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindLong(parameterIndex++, new_version)
          bindLong(parameterIndex++, computer_id?.let { ComputerSpecAdapter.computer_idAdapter.encode(it) })
          bindLong(parameterIndex++, expected_version)
          bindLong(parameterIndex++, ComputerSpecAdapter.idAdapter.encode(id))
        }
    return result
  }

  private inner class SelectComputerSpecByTokenQuery<out T : Any>(
    public val token: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-1_545_744_768, """
    |SELECT ComputerSpec.id, ComputerSpec.created_at, ComputerSpec.version, ComputerSpec.account_id, ComputerSpec.token, ComputerSpec.slug, ComputerSpec.computer_id
    |FROM
    |  ComputerSpec
    |WHERE
    |  token = ?
    |LIMIT 1
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, token)
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
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(1_134_491_967, """
    |INSERT INTO ComputerSpec(
    |  created_at,
    |  version,
    |  account_id,
    |  token,
    |  slug
    |)
    |VALUES (
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 5) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindObject(parameterIndex++, ComputerSpecAdapter.created_atAdapter.encode(created_at))
      bindLong(parameterIndex++, version)
      bindLong(parameterIndex++, ComputerSpecAdapter.account_idAdapter.encode(account_id))
      bindString(parameterIndex++, token)
      bindString(parameterIndex++, ComputerSpecAdapter.slugAdapter.encode(slug))
    }

    override fun toString(): String = "ComputerSpec.sq:insertComputerSpec"
  }
}

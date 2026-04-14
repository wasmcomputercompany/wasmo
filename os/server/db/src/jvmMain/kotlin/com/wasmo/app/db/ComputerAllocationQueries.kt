package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as JdbcCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.RealSqlPreparedStatement as JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Instant

public class ComputerAllocationQueries(
  private val driver: SqlDriver,
  private val ComputerAllocationAdapter: ComputerAllocation.Adapter,
) {
  public fun <T : Any> findComputerAllocationByStripeSubscriptionId(
    stripe_subscription_id: String,
    limit: Long,
    mapper: (
      id: ComputerAllocationId,
      created_at: Instant,
      version: Int,
      stripe_customer_id: StripeCustomerId,
      stripe_subscription_id: String,
      computer_id: ComputerId,
      active_start: Instant,
      active_end: Instant,
    ) -> T,
  ): Query<T> = FindComputerAllocationByStripeSubscriptionIdQuery(stripe_subscription_id, limit) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerAllocationAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS32(2)!!,
      ComputerAllocationAdapter.stripe_customer_idAdapter.decode(cursor.getS64(3)!!),
      cursor.getString(4)!!,
      ComputerAllocationAdapter.computer_idAdapter.decode(cursor.getS64(5)!!),
      cursor.getInstant(6)!!,
      cursor.getInstant(7)!!,
    )
  }

  public fun findComputerAllocationByStripeSubscriptionId(stripe_subscription_id: String, limit: Long): Query<ComputerAllocation> = findComputerAllocationByStripeSubscriptionId(stripe_subscription_id, limit, ::ComputerAllocation)

  public fun <T : Any> findComputerAllocationByComputerId(
    computer_id: ComputerId,
    limit: Long,
    mapper: (
      id: ComputerAllocationId,
      created_at: Instant,
      version: Int,
      stripe_customer_id: StripeCustomerId,
      stripe_subscription_id: String,
      computer_id: ComputerId,
      active_start: Instant,
      active_end: Instant,
    ) -> T,
  ): Query<T> = FindComputerAllocationByComputerIdQuery(computer_id, limit) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      ComputerAllocationAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS32(2)!!,
      ComputerAllocationAdapter.stripe_customer_idAdapter.decode(cursor.getS64(3)!!),
      cursor.getString(4)!!,
      ComputerAllocationAdapter.computer_idAdapter.decode(cursor.getS64(5)!!),
      cursor.getInstant(6)!!,
      cursor.getInstant(7)!!
    )
  }

  public fun findComputerAllocationByComputerId(computer_id: ComputerId, limit: Long): Query<ComputerAllocation> = findComputerAllocationByComputerId(computer_id, limit, ::ComputerAllocation)

  /**
   * @return The number of rows updated.
   */
  public suspend fun insertComputerAllocation(
    created_at: Instant,
    version: Int,
    stripe_customer_id: StripeCustomerId,
    stripe_subscription_id: String,
    computer_id: ComputerId,
    active_start: Instant,
    active_end: Instant,
  ): Long {
    val result = driver.execute(2_049_785_289, """
        |INSERT INTO ComputerAllocation(
        |  created_at,
        |  version,
        |  stripe_customer_id,
        |  stripe_subscription_id,
        |  computer_id,
        |  active_start,
        |  active_end
        |)
        |VALUES (
        |  $1,
        |  $2,
        |  $3,
        |  $4,
        |  $5,
        |  $6,
        |  $7
        |)
        """.trimMargin(), 7) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindInstant(parameterIndex++, created_at)
          bindInt(parameterIndex++, version)
          bindLong(parameterIndex++, ComputerAllocationAdapter.stripe_customer_idAdapter.encode(stripe_customer_id))
          bindString(parameterIndex++, stripe_subscription_id)
          bindLong(parameterIndex++, ComputerAllocationAdapter.computer_idAdapter.encode(computer_id))
          bindInstant(parameterIndex++, active_start)
          bindInstant(parameterIndex++, active_end)
        }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public suspend fun truncateComputerAllocation(
    new_version: Int,
    active_end: Instant,
    expected_version: Int,
    id: ComputerAllocationId,
  ): Long {
    val result = driver.execute(174_224_886, """
        |UPDATE ComputerAllocation
        |SET
        |  version = $1,
        |  active_end = $2
        |WHERE
        |  version = $3 AND
        |  id = $4
        """.trimMargin(), 4) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindInt(parameterIndex++, new_version)
          bindInstant(parameterIndex++, active_end)
          bindInt(parameterIndex++, expected_version)
          bindLong(parameterIndex++, ComputerAllocationAdapter.idAdapter.encode(id))
        }
    return result
  }

  private inner class FindComputerAllocationByStripeSubscriptionIdQuery<out T : Any>(
    public val stripe_subscription_id: String,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(1_140_511_013, """
    |SELECT ComputerAllocation.id, ComputerAllocation.created_at, ComputerAllocation.version, ComputerAllocation.stripe_customer_id, ComputerAllocation.stripe_subscription_id, ComputerAllocation.computer_id, ComputerAllocation.active_start, ComputerAllocation.active_end
    |FROM ComputerAllocation
    |WHERE
    |  stripe_subscription_id = $1
    |ORDER BY
    |  active_start DESC
    |LIMIT $2
    """.trimMargin(), mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, stripe_subscription_id)
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "ComputerAllocation.sq:findComputerAllocationByStripeSubscriptionId"
  }

  private inner class FindComputerAllocationByComputerIdQuery<out T : Any>(
    public val computer_id: ComputerId,
    public val limit: Long,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(-1_115_404_810, """
    |SELECT ComputerAllocation.id, ComputerAllocation.created_at, ComputerAllocation.version, ComputerAllocation.stripe_customer_id, ComputerAllocation.stripe_subscription_id, ComputerAllocation.computer_id, ComputerAllocation.active_start, ComputerAllocation.active_end
    |FROM ComputerAllocation
    |WHERE
    |  computer_id = $1
    |ORDER BY
    |  active_start DESC
    |LIMIT $2
    """.trimMargin(), mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, ComputerAllocationAdapter.computer_idAdapter.encode(computer_id))
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "ComputerAllocation.sq:findComputerAllocationByComputerId"
  }
}

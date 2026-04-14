package com.wasmo.app.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import java.time.OffsetDateTime
import kotlin.Any
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

public class ComputerAllocationQueries(
  driver: SqlDriver,
  private val ComputerAllocationAdapter: ComputerAllocation.Adapter,
) : TransacterImpl(driver) {
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
      ComputerAllocationAdapter.idAdapter.decode(cursor.getLong(0)!!),
      ComputerAllocationAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getInt(2)!!,
      ComputerAllocationAdapter.stripe_customer_idAdapter.decode(cursor.getLong(3)!!),
      cursor.getString(4)!!,
      ComputerAllocationAdapter.computer_idAdapter.decode(cursor.getLong(5)!!),
      ComputerAllocationAdapter.active_startAdapter.decode(cursor.getObject<OffsetDateTime>(6)!!),
      ComputerAllocationAdapter.active_endAdapter.decode(cursor.getObject<OffsetDateTime>(7)!!)
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
      ComputerAllocationAdapter.idAdapter.decode(cursor.getLong(0)!!),
      ComputerAllocationAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getInt(2)!!,
      ComputerAllocationAdapter.stripe_customer_idAdapter.decode(cursor.getLong(3)!!),
      cursor.getString(4)!!,
      ComputerAllocationAdapter.computer_idAdapter.decode(cursor.getLong(5)!!),
      ComputerAllocationAdapter.active_startAdapter.decode(cursor.getObject<OffsetDateTime>(6)!!),
      ComputerAllocationAdapter.active_endAdapter.decode(cursor.getObject<OffsetDateTime>(7)!!)
    )
  }

  public fun findComputerAllocationByComputerId(computer_id: ComputerId, limit: Long): Query<ComputerAllocation> = findComputerAllocationByComputerId(computer_id, limit, ::ComputerAllocation)

  /**
   * @return The number of rows updated.
   */
  public fun insertComputerAllocation(
    created_at: Instant,
    version: Int,
    stripe_customer_id: StripeCustomerId,
    stripe_subscription_id: String,
    computer_id: ComputerId,
    active_start: Instant,
    active_end: Instant,
  ): QueryResult<Long> {
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
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?,
        |  ?
        |)
        """.trimMargin(), 7) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindObject(parameterIndex++, ComputerAllocationAdapter.created_atAdapter.encode(created_at))
          bindInt(parameterIndex++, version)
          bindLong(parameterIndex++, ComputerAllocationAdapter.stripe_customer_idAdapter.encode(stripe_customer_id))
          bindString(parameterIndex++, stripe_subscription_id)
          bindLong(parameterIndex++, ComputerAllocationAdapter.computer_idAdapter.encode(computer_id))
          bindObject(parameterIndex++, ComputerAllocationAdapter.active_startAdapter.encode(active_start))
          bindObject(parameterIndex++, ComputerAllocationAdapter.active_endAdapter.encode(active_end))
        }
    notifyQueries(2_049_785_289) { emit ->
      emit("ComputerAllocation")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun truncateComputerAllocation(
    new_version: Int,
    active_end: Instant,
    expected_version: Int,
    id: ComputerAllocationId,
  ): QueryResult<Long> {
    val result = driver.execute(174_224_886, """
        |UPDATE ComputerAllocation
        |SET
        |  version = ?,
        |  active_end = ?
        |WHERE
        |  version = ? AND
        |  id = ?
        """.trimMargin(), 4) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindInt(parameterIndex++, new_version)
          bindObject(parameterIndex++, ComputerAllocationAdapter.active_endAdapter.encode(active_end))
          bindInt(parameterIndex++, expected_version)
          bindLong(parameterIndex++, ComputerAllocationAdapter.idAdapter.encode(id))
        }
    notifyQueries(174_224_886) { emit ->
      emit("ComputerAllocation")
    }
    return result
  }

  private inner class FindComputerAllocationByStripeSubscriptionIdQuery<out T : Any>(
    public val stripe_subscription_id: String,
    public val limit: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("ComputerAllocation", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("ComputerAllocation", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_140_511_013, """
    |SELECT ComputerAllocation.id, ComputerAllocation.created_at, ComputerAllocation.version, ComputerAllocation.stripe_customer_id, ComputerAllocation.stripe_subscription_id, ComputerAllocation.computer_id, ComputerAllocation.active_start, ComputerAllocation.active_end
    |FROM ComputerAllocation
    |WHERE
    |  stripe_subscription_id = ?
    |ORDER BY
    |  active_start DESC
    |LIMIT ?
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
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("ComputerAllocation", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("ComputerAllocation", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_115_404_810, """
    |SELECT ComputerAllocation.id, ComputerAllocation.created_at, ComputerAllocation.version, ComputerAllocation.stripe_customer_id, ComputerAllocation.stripe_subscription_id, ComputerAllocation.computer_id, ComputerAllocation.active_start, ComputerAllocation.active_end
    |FROM ComputerAllocation
    |WHERE
    |  computer_id = ?
    |ORDER BY
    |  active_start DESC
    |LIMIT ?
    """.trimMargin(), mapper, 2) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindLong(parameterIndex++, ComputerAllocationAdapter.computer_idAdapter.encode(computer_id))
      bindLong(parameterIndex++, limit)
    }

    override fun toString(): String = "ComputerAllocation.sq:findComputerAllocationByComputerId"
  }
}

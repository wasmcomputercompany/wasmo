package com.wasmo.app.db

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.JdbcCursor
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import com.wasmo.identifiers.StripeCustomerId
import java.time.OffsetDateTime
import kotlin.Any
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

public class StripeCustomerQueries(
  driver: SqlDriver,
  private val StripeCustomerAdapter: StripeCustomer.Adapter,
) : TransacterImpl(driver) {
  public fun insertStripeCustomer(
    created_at: Instant,
    version: Int,
    stripe_customer_id: String,
    name: String,
    email: String,
    country: String,
    postal_code: String,
  ): ExecutableQuery<StripeCustomerId> = InsertStripeCustomerQuery(created_at, version, stripe_customer_id, name, email, country, postal_code) { cursor ->
    check(cursor is JdbcCursor)
    StripeCustomerAdapter.idAdapter.decode(cursor.getLong(0)!!)
  }

  public fun <T : Any> findStripeCustomerByStripeCustomerId(stripe_customer_id: String, mapper: (
    id: StripeCustomerId,
    created_at: Instant,
    version: Int,
    stripe_customer_id: String,
    name: String,
    email: String,
    country: String,
    postal_code: String,
  ) -> T): Query<T> = FindStripeCustomerByStripeCustomerIdQuery(stripe_customer_id) { cursor ->
    check(cursor is JdbcCursor)
    mapper(
      StripeCustomerAdapter.idAdapter.decode(cursor.getLong(0)!!),
      StripeCustomerAdapter.created_atAdapter.decode(cursor.getObject<OffsetDateTime>(1)!!),
      cursor.getInt(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun findStripeCustomerByStripeCustomerId(stripe_customer_id: String): Query<StripeCustomer> = findStripeCustomerByStripeCustomerId(stripe_customer_id, ::StripeCustomer)

  /**
   * @return The number of rows updated.
   */
  public fun updateStripeCustomer(
    new_version: Int,
    name: String,
    email: String,
    country: String,
    postal_code: String,
    expected_version: Int,
    id: StripeCustomerId,
  ): QueryResult<Long> {
    val result = driver.execute(1_933_751_993, """
        |UPDATE StripeCustomer
        |SET
        |  version = ?,
        |  name = ?,
        |  email = ?,
        |  country = ?,
        |  postal_code = ?
        |WHERE
        |  version = ? AND
        |  id = ?
        """.trimMargin(), 7) {
          check(this is JdbcPreparedStatement)
          var parameterIndex = 0
          bindInt(parameterIndex++, new_version)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, email)
          bindString(parameterIndex++, country)
          bindString(parameterIndex++, postal_code)
          bindInt(parameterIndex++, expected_version)
          bindLong(parameterIndex++, StripeCustomerAdapter.idAdapter.encode(id))
        }
    notifyQueries(1_933_751_993) { emit ->
      emit("StripeCustomer")
    }
    return result
  }

  private inner class InsertStripeCustomerQuery<out T : Any>(
    public val created_at: Instant,
    public val version: Int,
    public val stripe_customer_id: String,
    public val name: String,
    public val email: String,
    public val country: String,
    public val postal_code: String,
    mapper: (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_145_248_937, """
    |INSERT INTO StripeCustomer(
    |  created_at,
    |  version,
    |  stripe_customer_id,
    |  name,
    |  email,
    |  country,
    |  postal_code
    |)
    |VALUES (
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?,
    |  ?
    |) RETURNING id
    """.trimMargin(), mapper, 7) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindObject(parameterIndex++, StripeCustomerAdapter.created_atAdapter.encode(created_at))
      bindInt(parameterIndex++, version)
      bindString(parameterIndex++, stripe_customer_id)
      bindString(parameterIndex++, name)
      bindString(parameterIndex++, email)
      bindString(parameterIndex++, country)
      bindString(parameterIndex++, postal_code)
    }.also {
      notifyQueries(1_145_248_937) { emit ->
        emit("StripeCustomer")
      }
    }

    override fun toString(): String = "StripeCustomer.sq:insertStripeCustomer"
  }

  private inner class FindStripeCustomerByStripeCustomerIdQuery<out T : Any>(
    public val stripe_customer_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Listener) {
      driver.addListener("StripeCustomer", listener = listener)
    }

    override fun removeListener(listener: Listener) {
      driver.removeListener("StripeCustomer", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_695_702_278, """
    |SELECT StripeCustomer.id, StripeCustomer.created_at, StripeCustomer.version, StripeCustomer.stripe_customer_id, StripeCustomer.name, StripeCustomer.email, StripeCustomer.country, StripeCustomer.postal_code
    |FROM StripeCustomer
    |WHERE
    |  stripe_customer_id = ?
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, stripe_customer_id)
    }

    override fun toString(): String = "StripeCustomer.sq:findStripeCustomerByStripeCustomerId"
  }
}

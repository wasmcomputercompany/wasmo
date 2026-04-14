package com.wasmo.app.db

import com.wasmo.app.db2.RealSqlCursor as JdbcCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import com.wasmo.app.db2.RealSqlPreparedStatement as JdbcPreparedStatement
import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.db.sqlservice.Query2 as ExecutableQuery
import com.wasmo.db.sqlservice.Query2 as Query
import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Instant

public class StripeCustomerQueries(
  private val driver: SqlDriver,
  private val StripeCustomerAdapter: StripeCustomer.Adapter,
) {
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
    StripeCustomerAdapter.idAdapter.decode(cursor.getS64(0)!!)
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
      StripeCustomerAdapter.idAdapter.decode(cursor.getS64(0)!!),
      cursor.getInstant(1)!!,
      cursor.getS32(2)!!,
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
  public suspend fun updateStripeCustomer(
    new_version: Int,
    name: String,
    email: String,
    country: String,
    postal_code: String,
    expected_version: Int,
    id: StripeCustomerId,
  ): Long {
    val result = driver.execute(1_933_751_993, """
        |UPDATE StripeCustomer
        |SET
        |  version = $1,
        |  name = $2,
        |  email = $3,
        |  country = $4,
        |  postal_code = $5
        |WHERE
        |  version = $6 AND
        |  id = $7
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
    mapper: suspend (SqlCursor) -> T,
  ) : ExecutableQuery<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(1_145_248_937, """
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
    |  $1,
    |  $2,
    |  $3,
    |  $4,
    |  $5,
    |  $6,
    |  $7
    |) RETURNING id
    """.trimMargin(), mapper, 7) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindInstant(parameterIndex++, created_at)
      bindInt(parameterIndex++, version)
      bindString(parameterIndex++, stripe_customer_id)
      bindString(parameterIndex++, name)
      bindString(parameterIndex++, email)
      bindString(parameterIndex++, country)
      bindString(parameterIndex++, postal_code)
    }

    override fun toString(): String = "StripeCustomer.sq:insertStripeCustomer"
  }

  private inner class FindStripeCustomerByStripeCustomerIdQuery<out T : Any>(
    public val stripe_customer_id: String,
    mapper: suspend (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R = driver.executeQuery(1_695_702_278, """
    |SELECT StripeCustomer.id, StripeCustomer.created_at, StripeCustomer.version, StripeCustomer.stripe_customer_id, StripeCustomer.name, StripeCustomer.email, StripeCustomer.country, StripeCustomer.postal_code
    |FROM StripeCustomer
    |WHERE
    |  stripe_customer_id = $1
    """.trimMargin(), mapper, 1) {
      check(this is JdbcPreparedStatement)
      var parameterIndex = 0
      bindString(parameterIndex++, stripe_customer_id)
    }

    override fun toString(): String = "StripeCustomer.sq:findStripeCustomerByStripeCustomerId"
  }
}

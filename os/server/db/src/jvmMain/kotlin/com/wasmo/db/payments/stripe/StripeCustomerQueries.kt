package com.wasmo.db.payments.stripe

import com.wasmo.db.bindStripeCustomerId
import com.wasmo.db.getStripeCustomerId
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun insertStripeCustomer(
  created_at: Instant,
  version: Int,
  stripe_customer_id: String,
  name: String,
  email: String,
  country: String,
  postal_code: String,
): StripeCustomerId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO StripeCustomer(
      created_at,
      version,
      stripe_customer_id,
      name,
      email,
      country,
      postal_code
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7
    ) RETURNING id
    """,
  ) {
    bindInstant(0, created_at)
    bindS32(1, version)
    bindString(2, stripe_customer_id)
    bindString(3, name)
    bindString(4, email)
    bindString(5, country)
    bindString(6, postal_code)
  }
  return rowIterator.single {
    getStripeCustomerId(0)
  }
}

context(connection: SqlConnection)
suspend fun findStripeCustomerByStripeCustomerId(stripe_customer_id: String): StripeCustomer? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      version,
      stripe_customer_id,
      name,
      email,
      country,
      postal_code
    FROM StripeCustomer
    WHERE stripe_customer_id = $1
    """,
  ) {
    bindString(0, stripe_customer_id)
  }
  return rowIterator.singleOrNull {
    StripeCustomer(
      getStripeCustomerId(0),
      getInstant(1)!!,
      getS32(2)!!,
      getString(3)!!,
      getString(4)!!,
      getString(5)!!,
      getString(6)!!,
      getString(7)!!,
    )
  }
}

context(connection: SqlConnection)
suspend fun updateStripeCustomer(
  new_version: Int,
  name: String,
  email: String,
  country: String,
  postal_code: String,
  expected_version: Int,
  id: StripeCustomerId,
): Long {
  return connection.execute(
    """
    UPDATE StripeCustomer
    SET
      version = $1,
      name = $2,
      email = $3,
      country = $4,
      postal_code = $5
    WHERE
      version = $6 AND
      id = $7
    """,
  ) {
    bindS32(0, new_version)
    bindString(1, name)
    bindString(2, email)
    bindString(3, country)
    bindString(4, postal_code)
    bindS32(5, expected_version)
    bindStripeCustomerId(6, id)
  }
}

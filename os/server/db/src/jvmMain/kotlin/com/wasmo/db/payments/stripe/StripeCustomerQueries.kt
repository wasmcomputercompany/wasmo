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
  createdAt: Instant,
  version: Int,
  stripeCustomerId: String,
  name: String,
  email: String,
  country: String,
  postalCode: String,
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
    bindInstant(0, createdAt)
    bindS32(1, version)
    bindString(2, stripeCustomerId)
    bindString(3, name)
    bindString(4, email)
    bindString(5, country)
    bindString(6, postalCode)
  }
  return rowIterator.single {
    getStripeCustomerId(0)
  }
}

context(connection: SqlConnection)
suspend fun findStripeCustomerByStripeCustomerId(
  stripeCustomerId: String,
): DbStripeCustomer? {
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
    bindString(0, stripeCustomerId)
  }
  return rowIterator.singleOrNull {
    DbStripeCustomer(
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
  newVersion: Int,
  name: String,
  email: String,
  country: String,
  postalCode: String,
  expectedVersion: Int,
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
    bindS32(0, newVersion)
    bindString(1, name)
    bindString(2, email)
    bindString(3, country)
    bindString(4, postalCode)
    bindS32(5, expectedVersion)
    bindStripeCustomerId(6, id)
  }
}

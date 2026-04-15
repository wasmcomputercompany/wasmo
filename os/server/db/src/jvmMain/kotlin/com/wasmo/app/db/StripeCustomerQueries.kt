package com.wasmo.app.db

import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

suspend fun SqlConnection.insertStripeCustomer(
  created_at: Instant,
  version: Int,
  stripe_customer_id: String,
  name: String,
  email: String,
  country: String,
  postal_code: String,
): StripeCustomerId {
  val rowIterator = executeQuery(
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
    var parameterIndex = 0
    bindInstant(parameterIndex++, created_at)
    bindS32(parameterIndex++, version)
    bindString(parameterIndex++, stripe_customer_id)
    bindString(parameterIndex++, name)
    bindString(parameterIndex++, email)
    bindString(parameterIndex++, country)
    bindString(parameterIndex++, postal_code)
  }
  return rowIterator.single { cursor ->
    cursor.getStripeCustomerId(0)
  }
}

suspend fun SqlConnection.findStripeCustomerByStripeCustomerId(stripe_customer_id: String): StripeCustomer? {
  val rowIterator = executeQuery(
    """
    SELECT StripeCustomer.id, StripeCustomer.created_at, StripeCustomer.version, StripeCustomer.stripe_customer_id, StripeCustomer.name, StripeCustomer.email, StripeCustomer.country, StripeCustomer.postal_code
    FROM StripeCustomer
    WHERE
      stripe_customer_id = $1
    """,
  ) {
    var parameterIndex = 0
    bindString(parameterIndex++, stripe_customer_id)
  }
  return rowIterator.singleOrNull { cursor ->
    StripeCustomer(
      cursor.getStripeCustomerId(0),
      cursor.getInstant(1)!!,
      cursor.getS32(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
    )
  }
}

suspend fun SqlConnection.updateStripeCustomer(
  new_version: Int,
  name: String,
  email: String,
  country: String,
  postal_code: String,
  expected_version: Int,
  id: StripeCustomerId,
): Long {
  return execute(
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
    var parameterIndex = 0
    bindS32(parameterIndex++, new_version)
    bindString(parameterIndex++, name)
    bindString(parameterIndex++, email)
    bindString(parameterIndex++, country)
    bindString(parameterIndex++, postal_code)
    bindS32(parameterIndex++, expected_version)
    bindStripeCustomerId(parameterIndex++, id)
  }
}

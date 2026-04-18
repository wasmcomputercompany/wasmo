package com.wasmo.emails

import com.wasmo.db.bindAccountId
import com.wasmo.db.getAccountId
import com.wasmo.db.getLinkedEmailAddressId
import com.wasmo.identifiers.AccountId
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun insertLinkedEmailAddress(
  createdAt: Instant,
  accountId: AccountId,
  emailAddress: String,
  active: Boolean,
): Long {
  return connection.execute(
    """
    INSERT INTO LinkedEmailAddress(
      created_at,
      account_id,
      email_address,
      active
    )
    VALUES (
      $1,
      $2,
      $3,
      $4
    )
    """,
  ) {
    bindInstant(0, createdAt)
    bindAccountId(1, accountId)
    bindString(2, emailAddress)
    bindBool(3, active)
  }
}

context(connection: SqlConnection)
suspend fun selectLinkedEmailAddressOrNull(
  emailAddress: String,
): LinkedEmailAddress? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      email_address,
      active
    FROM LinkedEmailAddress
    WHERE
      email_address = $1 AND
      active = $2
    """,
  ) {
    bindString(0, emailAddress)
    bindBool(1, true)
  }

  return rowIterator.singleOrNull {
    LinkedEmailAddress(
      id = getLinkedEmailAddressId(0),
      createdAt = getInstant(1)!!,
      accountId = getAccountId(2),
      emailAddress = getString(3)!!,
      active = getBool(4)!!,
    )
  }
}

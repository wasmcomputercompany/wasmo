package com.wasmo.app.db

import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun findComputerAllocationByStripeSubscriptionId(
  stripe_subscription_id: String,
  limit: Long,
): ComputerAllocation? {
  val rowIterator = connection.executeQuery(
    """
    SELECT ComputerAllocation.id, ComputerAllocation.created_at, ComputerAllocation.version, ComputerAllocation.stripe_customer_id, ComputerAllocation.stripe_subscription_id, ComputerAllocation.computer_id, ComputerAllocation.active_start, ComputerAllocation.active_end
    FROM ComputerAllocation
    WHERE
      stripe_subscription_id = $1
    ORDER BY
      active_start DESC
    LIMIT $2
    """,
  ) {
    var parameterIndex = 0
    bindString(parameterIndex++, stripe_subscription_id)
    bindS64(parameterIndex++, limit)
  }

  return rowIterator.singleOrNull { cursor ->
    ComputerAllocation(
      cursor.getComputerAllocationId(0),
      cursor.getInstant(1)!!,
      cursor.getS32(2)!!,
      cursor.getStripeCustomerId(3),
      cursor.getString(4)!!,
      cursor.getComputerId(5),
      cursor.getInstant(6)!!,
      cursor.getInstant(7)!!,
    )
  }
}

context(connection: SqlConnection)
suspend fun insertComputerAllocation(
  created_at: Instant,
  version: Int,
  stripe_customer_id: StripeCustomerId,
  stripe_subscription_id: String,
  computer_id: ComputerId,
  active_start: Instant,
  active_end: Instant,
): Long {
  return connection.execute(
    """
    INSERT INTO ComputerAllocation(
      created_at,
      version,
      stripe_customer_id,
      stripe_subscription_id,
      computer_id,
      active_start,
      active_end
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7
    )
    """,
  ) {
    var parameterIndex = 0
    bindInstant(parameterIndex++, created_at)
    bindS32(parameterIndex++, version)
    bindStripeCustomerId(parameterIndex++, stripe_customer_id)
    bindString(parameterIndex++, stripe_subscription_id)
    bindComputerId(parameterIndex++, computer_id)
    bindInstant(parameterIndex++, active_start)
    bindInstant(parameterIndex++, active_end)
  }
}

context(connection: SqlConnection)
suspend fun truncateComputerAllocation(
  new_version: Int,
  active_end: Instant,
  expected_version: Int,
  id: ComputerAllocationId,
): Long {
  return connection.execute(
    """
    UPDATE ComputerAllocation
    SET
      version = $1,
      active_end = $2
    WHERE
      version = $3 AND
      id = $4
    """,
  ) {
    var parameterIndex = 0
    bindS32(parameterIndex++, new_version)
    bindInstant(parameterIndex++, active_end)
    bindS32(parameterIndex++, expected_version)
    bindComputerAllocationId(parameterIndex++, id)
  }
}

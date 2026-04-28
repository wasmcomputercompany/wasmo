package com.wasmo.db.computers

import com.wasmo.db.bindComputerAllocationId
import com.wasmo.db.bindComputerId
import com.wasmo.db.bindStripeCustomerId
import com.wasmo.db.getComputerAllocationId
import com.wasmo.db.getComputerId
import com.wasmo.db.getStripeCustomerId
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
): DbComputerAllocation? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      ComputerAllocation.id,
      ComputerAllocation.created_at,
      ComputerAllocation.version,
      ComputerAllocation.stripe_customer_id,
      ComputerAllocation.stripe_subscription_id,
      ComputerAllocation.computer_id,
      ComputerAllocation.active_start,
      ComputerAllocation.active_end
    FROM ComputerAllocation
    WHERE stripe_subscription_id = $1
    ORDER BY active_start DESC
    LIMIT $2
    """,
  ) {
    bindString(0, stripe_subscription_id)
    bindS64(1, limit)
  }

  return rowIterator.singleOrNull {
    DbComputerAllocation(
      getComputerAllocationId(0),
      getInstant(1)!!,
      getS32(2)!!,
      getStripeCustomerId(3),
      getString(4)!!,
      getComputerId(5),
      getInstant(6)!!,
      getInstant(7)!!,
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
    bindInstant(0, created_at)
    bindS32(1, version)
    bindStripeCustomerId(2, stripe_customer_id)
    bindString(3, stripe_subscription_id)
    bindComputerId(4, computer_id)
    bindInstant(5, active_start)
    bindInstant(6, active_end)
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
    bindS32(0, new_version)
    bindInstant(1, active_end)
    bindS32(2, expected_version)
    bindComputerAllocationId(3, id)
  }
}

package com.wasmo.db.computers

import com.wasmo.db.bindComputerAllocationId
import com.wasmo.db.bindComputerId
import com.wasmo.db.bindJson
import com.wasmo.db.bindStripeCustomerId
import com.wasmo.db.computers.ComputerAllocationType.*
import com.wasmo.db.decodeJson
import com.wasmo.db.getComputerAllocationId
import com.wasmo.db.getComputerId
import com.wasmo.db.getStripeCustomerId
import com.wasmo.identifiers.ComputerAllocationId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.StripeCustomerId
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import wasmo.sql.SqlConnection
import wasmox.sql.singleOrNull

context(connection: SqlConnection)
suspend fun findStripeComputerAllocationBySubscriptionId(
  stripe_subscription_id: String,
  limit: Long,
): DbComputerAllocation? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      type,
      created_at,
      version,
      stripe_customer_id,
      stripe_subscription_id,
      computer_id,
      active_start,
      active_end
    FROM ComputerAllocation
    WHERE stripe_subscription_id = $1 AND type = $2
    ORDER BY active_start DESC
    LIMIT $3
    """,
  ) {
    bindString(0, stripe_subscription_id)
    bindJson<ComputerAllocationType>(1, ComputerAllocationType.Stripe)
    bindS64(2, limit)
  }

  return rowIterator.singleOrNull {
    DbComputerAllocation(
      getComputerAllocationId(0),
      decodeJson<ComputerAllocationType>(1),
      getInstant(2)!!,
      getS32(3)!!,
      getStripeCustomerId(4),
      getString(5)!!,
      getComputerId(6),
      getInstant(7)!!,
      getInstant(8)!!,
    )
  }
}

context(connection: SqlConnection)
suspend fun insertFreeComputerAllocation(
  created_at: Instant,
  version: Int,
  computer_id: ComputerId,
  active_start: Instant,
): Long = insertComputerAllocation(
  type = Free,
  created_at = created_at,
  version = version,
  stripe_customer_id = null,
  stripe_subscription_id = null,
  computer_id = computer_id,
  active_start = active_start,
  active_end = null,
)

context(connection: SqlConnection)
suspend fun insertStripeComputerAllocation(
  created_at: Instant,
  version: Int,
  stripe_customer_id: StripeCustomerId,
  stripe_subscription_id: String,
  computer_id: ComputerId,
  active_start: Instant,
  active_end: Instant,
): Long = insertComputerAllocation(
    type = Stripe,
    created_at = created_at,
    version = version,
    stripe_customer_id = stripe_customer_id,
    stripe_subscription_id = stripe_subscription_id,
    computer_id = computer_id,
    active_start = active_start,
    active_end = active_end,
  )

context(connection: SqlConnection)
private suspend fun insertComputerAllocation(
  type: ComputerAllocationType,
  created_at: Instant,
  version: Int,
  stripe_customer_id: StripeCustomerId?,
  stripe_subscription_id: String?,
  computer_id: ComputerId,
  active_start: Instant,
  active_end: Instant?,
): Long {
  return connection.execute(
    """
    INSERT INTO ComputerAllocation(
      type,
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
      $7,
      $8
    )
    """,
  ) {
    bindJson<ComputerAllocationType>(0, type)
    bindInstant(1, created_at)
    bindS32(2, version)
    bindStripeCustomerId(3, stripe_customer_id)
    bindString(4, stripe_subscription_id)
    bindComputerId(5, computer_id)
    bindInstant(6, active_start)
    bindInstant(7, active_end)
  }
}

context(connection: SqlConnection)
suspend fun truncateStripeComputerAllocation(
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
      id = $4 AND
      type = $5
    """,
  ) {
    bindS32(0, new_version)
    bindInstant(1, active_end)
    bindS32(2, expected_version)
    bindComputerAllocationId(3, id)
    bindJson<ComputerAllocationType>(4, Stripe)
  }
}

@Serializable
enum class ComputerAllocationType {
  @SerialName("free")
  Free,
  @SerialName("stripe")
  Stripe,
}

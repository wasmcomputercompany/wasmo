package com.wasmo.permits

import com.wasmo.db.getPermitId
import com.wasmo.identifiers.PermitId
import com.wasmo.identifiers.PermitType
import com.wasmo.sql.list
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

/**
 * Returns up to [RateLimit.count] permits since `(now - [RateLimit.duration])`. If this result is
 * fewer than [RateLimit.count], a permit should be acquired. Otherwise, no permit is available.
 */
context(connection: SqlConnection)
suspend fun selectPrecedingPermits(
  now: Instant,
  type: PermitType,
  value: String,
  rateLimit: RateLimit,
): List<Permit> {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      type,
      value,
      count,
      serial_number,
      acquire_at
    FROM Permit
    WHERE
      type = $1 AND
      value = $2 AND
      acquire_at > $3
    ORDER BY
      serial_number DESC
    """,
  ) {
    bindString(0, type.value)
    bindString(1, value)
    bindInstant(2, now - rateLimit.duration)
  }

  return rowIterator.list {
    getPermit()
  }
}

context(connection: SqlConnection)
suspend fun selectLatestPermit(
  type: PermitType,
  value: String,
): Permit? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      type,
      value,
      count,
      serial_number,
      acquire_at
    FROM Permit
    WHERE
      type = $1 AND
      value = $2
    ORDER BY
      serial_number DESC
    LIMIT $3
    """,
  ) {
    bindString(0, type.value)
    bindString(1, value)
    bindS32(2, 1)
  }

  return rowIterator.singleOrNull {
    getPermit()
  }
}

context(connection: SqlConnection)
suspend fun insertPermit(
  type: PermitType,
  value: String,
  count: Long,
  serialNumber: Long,
  acquireAt: Instant,
): PermitId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO Permit(
      type,
      value,
      count,
      serial_number,
      acquire_at
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5
    ) RETURNING id
    """,
  ) {
    bindString(0, type.value)
    bindString(1, value)
    bindS64(2, count)
    bindS64(3, serialNumber)
    bindInstant(4, acquireAt)
  }

  return rowIterator.single {
    getPermitId(0)
  }
}

private fun SqlRow.getPermit() = Permit(
  id = getPermitId(0),
  type = PermitType(getString(1)!!),
  value = getString(2)!!,
  count = getS64(3)!!,
  serialNumber = getS64(4)!!,
  acquireAt = getInstant(5)!!,
)

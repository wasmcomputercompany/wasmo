package com.wasmo.permits

import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.PermitType
import com.wasmo.permits.PermitService.Hook
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import wasmo.sql.SqlConnection

/**
 * A very simplistic permit issuing system.
 *
 * TODO: build a mechanism to sweep old permits.
 */
@Inject
@SingleIn(OsScope::class)
class RealPermitService : PermitService {
  context(sqlConnection: SqlConnection)
  override suspend fun tryAcquire(
    now: Instant,
    type: PermitType,
    value: String,
    count: Long,
    rateLimit: RateLimit,
    hook: Hook?,
  ): Boolean {
    val permits = selectPrecedingPermits(
      now = now,
      type = type,
      value = value,
      rateLimit = rateLimit,
    )
    if (count > 0L && permits.sumOf { it.count } >= rateLimit.count) return false

    val latestPermit = permits.firstOrNull()
      ?: selectLatestPermit(
        type = type,
        value = value,
      )

    val serialNumber = when {
      latestPermit != null -> latestPermit.serialNumber + 1L
      else -> 1L
    }

    hook?.beforeAcquire(serialNumber)

    insertPermit(
      type = type,
      value = value,
      count = count,
      serialNumber = serialNumber,
      acquireAt = now,
    )

    return true
  }
}

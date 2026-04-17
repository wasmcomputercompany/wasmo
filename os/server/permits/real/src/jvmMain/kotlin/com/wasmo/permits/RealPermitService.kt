package com.wasmo.permits

import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.PermitType
import com.wasmo.sql.transaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase

/**
 * A very simplistic permit issuing system.
 *
 * TODO: build a mechanism to sweep old permits.
 */
@Inject
@SingleIn(OsScope::class)
class RealPermitService(
  val clock: Clock,
  val wasmoDb: SqlDatabase,
) : PermitService {
  override suspend fun tryAcquire(
    type: PermitType,
    value: String,
    rateLimit: RateLimit,
  ): Boolean = tryAcquireWithHook(type, value, rateLimit)

  suspend fun tryAcquireWithHook(
    type: PermitType,
    value: String,
    rateLimit: RateLimit,
    hook: Hook? = null,
  ): Boolean {
    val now = clock.now()
    return wasmoDb.transaction(attemptCount = 3) {
      val permits = selectPrecedingPermits(
        now = now,
        type = type,
        value = value,
        rateLimit = rateLimit,
      )
      if (permits.size >= rateLimit.count) return@transaction false

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
        serialNumber = serialNumber,
        acquireAt = now,
      )

      return@transaction true
    }
  }

  /** For testing, this is used to force races that wouldn't occur organically. */
  interface Hook {
    suspend fun beforeAcquire(serialNumber: Long)
  }
}

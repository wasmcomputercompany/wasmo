package com.wasmo.permits

import com.wasmo.identifiers.PermitType
import kotlin.time.Duration
import kotlin.time.Instant
import wasmo.sql.SqlConnection

interface PermitService {
  /**
   * @param count the number of permits to acquire. Use -1 to release a permit, typically after a
   *   successful action that shouldn't consume a permit.
   */
  context(sqlConnection: SqlConnection)
  suspend fun tryAcquire(
    now: Instant,
    type: PermitType,
    value: String,
    count: Long = 1L,
    rateLimit: RateLimit,
    hook: Hook? = null,
  ): Boolean

  /** For testing, this is used to force races that wouldn't occur organically. */
  interface Hook {
    suspend fun beforeAcquire(serialNumber: Long)
  }
}

data class RateLimit(
  val count: Int,
  val duration: Duration,
)

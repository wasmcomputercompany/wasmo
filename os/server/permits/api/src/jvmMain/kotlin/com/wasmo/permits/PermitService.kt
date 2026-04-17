package com.wasmo.permits

import com.wasmo.identifiers.PermitType
import kotlin.time.Duration

interface PermitService {
  /** Returns true if the permit was acquired. */
  suspend fun tryAcquire(
    type: PermitType,
    value: String,
    rateLimit: RateLimit,
  ): Boolean
}

data class RateLimit(
  val count: Int,
  val duration: Duration,
)

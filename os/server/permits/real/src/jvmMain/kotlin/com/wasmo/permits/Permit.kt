package com.wasmo.permits

import com.wasmo.identifiers.PermitId
import com.wasmo.identifiers.PermitType
import kotlin.time.Instant

data class Permit(
  val id: PermitId,
  val type: PermitType,
  val value: String,
  val serialNumber: Long,
  val acquireAt: Instant,
)

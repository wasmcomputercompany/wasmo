package com.wasmo.permits

import com.wasmo.identifiers.PermitId
import com.wasmo.identifiers.PermitType
import kotlin.time.Instant

data class DbPermit(
  val id: PermitId,
  val type: PermitType,
  val value: String,
  val count: Long,
  val serialNumber: Long,
  val acquireAt: Instant,
)

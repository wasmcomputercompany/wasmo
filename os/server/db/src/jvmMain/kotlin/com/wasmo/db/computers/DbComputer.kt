package com.wasmo.db.computers

import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import kotlin.time.Instant

data class DbComputer(
  val id: ComputerId,
  val createdAt: Instant,
  val version: Long,
  val slug: ComputerSlug,
)

package com.wasmo.app.db

import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import kotlin.time.Instant

data class Computer(
  val id: ComputerId,
  val created_at: Instant,
  val version: Long,
  val slug: ComputerSlug,
)

package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class Computer(
  val id: ComputerId,
  val created_at: Instant,
  val version: Long,
  val slug: ComputerSlug,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<ComputerId, Long>,
    val created_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val slugAdapter: ColumnAdapter<ComputerSlug, String>,
  )
}

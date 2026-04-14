package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import java.time.OffsetDateTime
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class InstalledApp(
  val id: InstalledAppId,
  val installed_at: Instant,
  val computer_id: ComputerId,
  val slug: AppSlug,
  val active: Boolean?,
  val version: Long,
  val wasmo_file_address: WasmoFileAddress,
  val active_release_id: InstalledAppReleaseId?,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<InstalledAppId, Long>,
    val installed_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val computer_idAdapter: ColumnAdapter<ComputerId, Long>,
    val slugAdapter: ColumnAdapter<AppSlug, String>,
    val wasmo_file_addressAdapter: ColumnAdapter<WasmoFileAddress, String>,
    val active_release_idAdapter: ColumnAdapter<InstalledAppReleaseId, Long>,
  )
}

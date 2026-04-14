package com.wasmo.app.db

import app.cash.sqldelight.ColumnAdapter
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String
import kotlin.time.Instant

data class InstalledAppRelease(
  val id: InstalledAppReleaseId,
  val first_active_at: Instant,
  val computer_id: ComputerId,
  val installed_app_id: InstalledAppId,
  val app_version: Long,
  val app_manifest_data: AppManifest,
) {
  class Adapter(
    val idAdapter: ColumnAdapter<InstalledAppReleaseId, Long>,
    val first_active_atAdapter: ColumnAdapter<Instant, OffsetDateTime>,
    val computer_idAdapter: ColumnAdapter<ComputerId, Long>,
    val installed_app_idAdapter: ColumnAdapter<InstalledAppId, Long>,
    val app_manifest_dataAdapter: ColumnAdapter<AppManifest, String>,
  )
}

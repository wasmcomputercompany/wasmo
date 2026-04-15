package com.wasmo.app.db

import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant

data class InstalledAppRelease(
  val id: InstalledAppReleaseId,
  val first_active_at: Instant,
  val computer_id: ComputerId,
  val installed_app_id: InstalledAppId,
  val app_version: Long,
  val app_manifest_data: AppManifest,
)

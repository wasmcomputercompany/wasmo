package com.wasmo.app.db

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
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
)

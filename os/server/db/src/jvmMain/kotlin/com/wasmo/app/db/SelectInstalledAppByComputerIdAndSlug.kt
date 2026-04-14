package com.wasmo.app.db

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import kotlin.Boolean
import kotlin.Long
import kotlin.time.Instant

data class SelectInstalledAppByComputerIdAndSlug(
  val id: InstalledAppId,
  val installed_at: Instant,
  val computer_id: ComputerId,
  val slug: AppSlug,
  val active: Boolean?,
  val version: Long,
  val wasmo_file_address: WasmoFileAddress,
  val active_release_id: InstalledAppReleaseId?,
  val id_: InstalledAppReleaseId?,
  val first_active_at: Instant?,
  val computer_id_: ComputerId?,
  val installed_app_id: InstalledAppId?,
  val app_version: Long?,
  val app_manifest_data: AppManifest?,
)

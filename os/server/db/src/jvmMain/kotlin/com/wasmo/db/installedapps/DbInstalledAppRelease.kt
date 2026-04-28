package com.wasmo.db.installedapps

import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.packaging.AppManifest
import kotlin.time.Instant

data class DbInstalledAppRelease(
  val id: InstalledAppReleaseId,
  val firstActiveAt: Instant,
  val computerId: ComputerId,
  val installedAppId: InstalledAppId,
  val appVersion: Long,
  val appManifestData: AppManifest,
)

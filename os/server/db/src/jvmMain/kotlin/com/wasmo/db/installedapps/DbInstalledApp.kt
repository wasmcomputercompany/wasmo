package com.wasmo.db.installedapps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppReleaseId
import com.wasmo.identifiers.WasmoFileAddress
import kotlin.time.Instant

data class DbInstalledApp(
  val id: InstalledAppId,
  val installedAt: Instant,
  val computerId: ComputerId,
  val slug: AppSlug,
  val active: Boolean?,
  val version: Long,
  val wasmoFileAddress: WasmoFileAddress,
  val activeReleaseId: InstalledAppReleaseId?,
)

package com.wasmo.db.installedapps

/** Join the [InstalledApp] and [InstalledAppRelease] tables. */
data class InstalledAppAndRelease(
  val installedApp: InstalledApp,
  val installedAppRelease: InstalledAppRelease?,
)

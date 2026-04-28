package com.wasmo.db.installedapps

/** Join the [DbInstalledApp] and [DbInstalledAppRelease] tables. */
data class DbInstalledAppAndRelease(
  val installedApp: DbInstalledApp,
  val installedAppRelease: DbInstalledAppRelease?,
)

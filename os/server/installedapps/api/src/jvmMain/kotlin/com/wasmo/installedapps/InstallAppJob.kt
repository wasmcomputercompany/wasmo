package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val installedAppId: InstalledAppId,
)

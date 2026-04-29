package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.JobName
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val installedAppId: InstalledAppId,
) {
  companion object {
    val JobName = JobName<InstallAppJob, Unit>("InstallAppJob")
  }
}

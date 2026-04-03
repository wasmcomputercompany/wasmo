package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.Job
import com.wasmo.identifiers.JobHandlerId
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val installedAppId: InstalledAppId,
) : Job {
  override val handlerId: JobHandlerId<InstallAppJob>
    get() = HandlerId

  companion object {
    val HandlerId = object : JobHandlerId<InstallAppJob> {
      override val serializer = serializer()
    }
  }
}

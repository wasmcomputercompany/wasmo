package com.wasmo.installedapps

import com.wasmo.identifiers.HandlerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.Job
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val installedAppId: InstalledAppId,
) : Job {
  override val handlerId: HandlerId<InstallAppJob>
    get() = HandlerId

  companion object {
    val HandlerId = object : HandlerId<InstallAppJob> {
      override val serializer = InstallAppJob.serializer()
    }
  }
}

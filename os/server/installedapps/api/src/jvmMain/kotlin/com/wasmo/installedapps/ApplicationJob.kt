package com.wasmo.installedapps

import com.wasmo.api.Base64UrlSerializer
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.JobName
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import okio.ByteString

@Serializable
data class ApplicationJob(
  val installedAppId: InstalledAppId,
  val queueName: String,
  val data: @Serializable(Base64UrlSerializer::class) ByteString,
  val executeAt: Instant?,
) {
  companion object {
    val JobName = JobName<ApplicationJob, Unit>("ApplicationJob")
  }
}

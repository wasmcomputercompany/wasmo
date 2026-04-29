package com.wasmo.installedapps

import com.wasmo.api.Base64UrlSerializer
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.Job
import com.wasmo.identifiers.JobHandlerId
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import okio.ByteString

@Serializable
data class ApplicationJob(
  val installedAppId: InstalledAppId,
  val queueName: String,
  val data: @Serializable(Base64UrlSerializer::class) ByteString,
  val executeAt: Instant?,
) : Job {
  override val handlerId: JobHandlerId<ApplicationJob>
    get() = HandlerId

  companion object {
    val HandlerId = object : JobHandlerId<ApplicationJob> {
      override val serializer = serializer()
    }
  }
}

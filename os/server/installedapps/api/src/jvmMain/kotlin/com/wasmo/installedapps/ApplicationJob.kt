package com.wasmo.installedapps

import com.wasmo.api.Base64UrlSerializer
import com.wasmo.identifiers.HandlerId
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.Job
import kotlinx.serialization.Serializable
import okio.ByteString

@Serializable
data class ApplicationJob(
  val installedAppId: InstalledAppId,
  val queueName: String,
  val data: @Serializable(Base64UrlSerializer::class) ByteString,
) : Job {
  override val handlerId: HandlerId<ApplicationJob>
    get() = HandlerId

  companion object {
    val HandlerId = object : HandlerId<ApplicationJob> {
      override val serializer = serializer()
    }
  }
}

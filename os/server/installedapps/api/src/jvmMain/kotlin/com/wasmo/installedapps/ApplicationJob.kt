package com.wasmo.installedapps

import com.wasmo.api.Base64UrlSerializer
import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import okio.ByteString

@Serializable
data class ApplicationJob(
  val installedAppId: InstalledAppId,
  val queueName: String,
  val data: @Serializable(Base64UrlSerializer::class) ByteString,
  val executeAt: Instant?,
)

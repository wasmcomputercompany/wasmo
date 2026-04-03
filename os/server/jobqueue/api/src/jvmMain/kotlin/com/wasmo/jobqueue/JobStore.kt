package com.wasmo.jobqueue

import com.wasmo.api.Base64UrlSerializer
import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import okio.ByteString

interface JobStore {
  fun enqueue(job: Job, executeAt: Instant? = null)
  fun cancel(job: Job)

  interface Handler<J : Job> {
    suspend fun execute(job: J)
  }
}

interface JobQueueEventListener {
  fun jobEnqueued(instant: Instant?)
  fun jobCompleted()

  companion object {
    val None = object : JobQueueEventListener {
      override fun jobEnqueued(instant: Instant?) {
      }

      override fun jobCompleted() {
      }
    }
  }
}

interface HandlerId<J : Job> {
  val serializer: KSerializer<J>

  object Application : HandlerId<ApplicationJob> {
    override val serializer = ApplicationJob.serializer()
  }

  object InstallApp : HandlerId<InstallAppJob> {
    override val serializer = InstallAppJob.serializer()
  }
}

interface Job {
  val handlerId: HandlerId<*>
}

@Serializable
data class ApplicationJob(
  val installedAppId: InstalledAppId,
  val queueName: String,
  val data: @Serializable(Base64UrlSerializer::class) ByteString,
) : Job {
  override val handlerId: HandlerId<ApplicationJob>
    get() = HandlerId.Application
}

@Serializable
data class InstallAppJob(
  val installedAppId: InstalledAppId,
) : Job {
  override val handlerId: HandlerId<InstallAppJob>
    get() = HandlerId.InstallApp
}

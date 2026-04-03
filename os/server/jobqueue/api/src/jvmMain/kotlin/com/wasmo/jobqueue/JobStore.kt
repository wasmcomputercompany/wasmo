package com.wasmo.jobqueue

import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import okio.ByteString

interface JobStore {
  fun enqueue(job: Job, executeAt: Instant? = null)
  fun cancel(job: Job)

  interface Handler<J : Job> {
    /** Returns the launched coroutines job, or null if the job could not be launched. */
    context(scope: CoroutineScope)
    suspend fun execute(job: J): kotlinx.coroutines.Job?
  }
}

sealed interface Job {
  val handlerId: HandlerId

  data class OsJob<T>(
    override val handlerId: HandlerId,
    val computerId: ComputerId,
    val data: T,
  ) : Job

  data class ApplicationJob(
    val installedAppId: InstalledAppId,
    val queueName: String,
    val data: ByteString,
  ) : Job {
    override val handlerId: HandlerId
      get() = HandlerId.Application
  }
}

enum class HandlerId {
  Application,
}

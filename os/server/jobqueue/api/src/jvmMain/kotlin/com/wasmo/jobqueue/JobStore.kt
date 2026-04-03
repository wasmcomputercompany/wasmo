package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import okio.ByteString

interface JobStore {
  fun enqueue(
    installedAppId: InstalledAppId,
    queueName: String,
    job: ByteString,
    executeAt: Instant?,
  )

  fun cancel(
    installedAppId: InstalledAppId,
    queueName: String,
    job: ByteString,
  )

  interface Handler {
    /** Returns the launched coroutines job, or null if the job could not be launched. */
    context(scope: CoroutineScope)
    suspend fun execute(
      installedAppId: InstalledAppId,
      queueName: String,
      job: ByteString,
    ): Job?
  }
}

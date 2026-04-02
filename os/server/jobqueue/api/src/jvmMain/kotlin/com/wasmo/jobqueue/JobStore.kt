package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface JobStore {
  fun enqueue(installedAppId: InstalledAppId, jobId: Long, executeAt: Instant?)
  fun cancel(installedAppId: InstalledAppId, jobId: Long)

  interface Handler {
    /** Returns the launched coroutines job, or null if the job could not be launched. */
    context(scope: CoroutineScope)
    suspend fun execute(installedAppId: InstalledAppId, jobId: Long): Job?
  }
}

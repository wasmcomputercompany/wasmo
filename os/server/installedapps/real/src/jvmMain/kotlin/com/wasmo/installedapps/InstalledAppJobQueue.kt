package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.jobqueue.JobStore
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import wasmo.jobs.JobQueue

@Inject
@SingleIn(InstalledAppScope::class)
class InstalledAppJobQueue(
  val jobStore: JobStore,
  val installedAppId: InstalledAppId,
) : JobQueue {
  override fun enqueue(jobId: Long, executeAt: Instant?) {
    jobStore.enqueue(installedAppId, jobId, executeAt)
  }

  override fun cancel(jobId: Long) {
    jobStore.cancel(installedAppId, jobId)
  }
}

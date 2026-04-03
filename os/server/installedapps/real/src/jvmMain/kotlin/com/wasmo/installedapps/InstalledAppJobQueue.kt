package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.jobs.JobStore
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import okio.ByteString
import wasmo.jobs.JobQueue

class InstalledAppJobQueue private constructor(
  val jobStore: JobStore,
  val installedAppId: InstalledAppId,
  val queueName: String,
) : JobQueue {
  override fun enqueue(job: ByteString, executeAt: Instant?) {
    jobStore.enqueue(ApplicationJob(installedAppId, queueName, job), executeAt)
  }

  override fun cancel(job: ByteString) {
    jobStore.cancel(ApplicationJob(installedAppId, queueName, job))
  }

  @Inject
  @SingleIn(InstalledAppScope::class)
  class Factory(
    val jobStore: JobStore,
    val installedAppId: InstalledAppId,
  ) : JobQueue.Factory {
    override fun get(name: String) = InstalledAppJobQueue(
      jobStore = jobStore,
      installedAppId = installedAppId,
      queueName = name,
    )
  }
}

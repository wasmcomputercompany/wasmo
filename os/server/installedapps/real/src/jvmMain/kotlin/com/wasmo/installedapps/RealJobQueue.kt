package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import okio.ByteString
import wasmo.jobs.JobQueue

/**
 * Adapts the platform's installed app [JobQueue] to the OS job queue.
 */
class RealJobQueue private constructor(
  private val osJobQueue: OsJobQueue,
  private val installedAppId: InstalledAppId,
  private val queueName: String,
) : JobQueue {
  override fun enqueue(job: ByteString, executeAt: Instant?) {
    osJobQueue.enqueue(ApplicationJob(installedAppId, queueName, job), executeAt)
  }

  override fun cancel(job: ByteString) {
    osJobQueue.cancel(ApplicationJob(installedAppId, queueName, job))
  }

  @Inject
  @SingleIn(InstalledAppScope::class)
  class Factory(
    val osJobQueue: OsJobQueue,
    val installedAppId: InstalledAppId,
  ) : JobQueue.Factory {
    override fun get(name: String) = RealJobQueue(
      osJobQueue = osJobQueue,
      installedAppId = installedAppId,
      queueName = name,
    )
  }
}

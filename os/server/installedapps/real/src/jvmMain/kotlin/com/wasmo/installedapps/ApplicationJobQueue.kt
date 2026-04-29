package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.jobs.OsJobQueue
import com.wasmo.sql.transaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import okio.ByteString
import wasmo.jobs.JobQueue
import wasmo.sql.SqlDatabase

/**
 * Adapts the platform's installed app [JobQueue] to the OS job queue.
 */
class ApplicationJobQueue private constructor(
  private val osJobQueue: OsJobQueue,
  private val installedAppId: InstalledAppId,
  private val wasmoDb: SqlDatabase,
  private val queueName: String,
) : JobQueue {


  override suspend fun enqueue(job: ByteString, executeAt: Instant?) {
    wasmoDb.transaction {
      osJobQueue.enqueue(
        ApplicationJob.JobName,
        ApplicationJob(installedAppId, queueName, job, executeAt),
      )
    }
  }

  override suspend fun cancel(job: ByteString) {
    wasmoDb.transaction {
      osJobQueue.cancel(
        ApplicationJob.JobName,
        ApplicationJob(installedAppId, queueName, job, null),
      )
    }
  }

  @Inject
  @SingleIn(InstalledAppScope::class)
  class Factory(
    val osJobQueue: OsJobQueue,
    val installedAppId: InstalledAppId,
    val wasmoDb: SqlDatabase,
  ) : JobQueue.Factory {
    override fun get(name: String) = ApplicationJobQueue(
      osJobQueue = osJobQueue,
      installedAppId = installedAppId,
      wasmoDb = wasmoDb,
      queueName = name,
    )
  }
}

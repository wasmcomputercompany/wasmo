package com.wasmo.installedapps

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.identifiers.InstalledAppScope
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import okio.ByteString
import wasmo.jobs.JobQueue
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

/**
 * Adapts the platform's installed app [JobQueue] to the OS job queue.
 */
class ApplicationJobQueue private constructor(
  private val jobQueue: OsJobQueue<ApplicationJob>,
  private val installedAppId: InstalledAppId,
  private val wasmoDb: SqlDatabase,
  private val queueName: String,
) : JobQueue {


  override suspend fun enqueue(job: ByteString, executeAt: Instant?) {
    wasmoDb.transaction {
      jobQueue.enqueue(
        ApplicationJob(installedAppId, queueName, job, executeAt),
      )
    }
  }

  override suspend fun cancel(job: ByteString) {
    wasmoDb.transaction {
      jobQueue.cancel(
        ApplicationJob(installedAppId, queueName, job, null),
      )
    }
  }

  @Inject
  @SingleIn(InstalledAppScope::class)
  class Factory(
    val jobQueue: OsJobQueue<ApplicationJob>,
    val installedAppId: InstalledAppId,
    val wasmoDb: SqlDatabase,
  ) : JobQueue.Factory {
    override fun get(name: String) = ApplicationJobQueue(
      jobQueue = jobQueue,
      installedAppId = installedAppId,
      wasmoDb = wasmoDb,
      queueName = name,
    )
  }
}

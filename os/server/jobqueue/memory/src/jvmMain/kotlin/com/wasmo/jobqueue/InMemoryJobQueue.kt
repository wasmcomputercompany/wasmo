package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.supervisorScope

/**
 * A simple job queue.
 */
@Inject
@SingleIn(AppScope::class)
class MemoryJobStore(
  private val clock: Clock,
  private val handler: JobStore.Handler,
) : JobStore {
  private val entry = LinkedBlockingQueue<Entry>()

  override fun enqueue(
    installedAppId: InstalledAppId,
    jobId: Long,
    executeAt: Instant?,
  ) {
    cancel(installedAppId, jobId)
    entry += Entry(
      installedAppId = installedAppId,
      jobId = jobId,
      executeAt = executeAt,
    )
  }

  override fun cancel(
    installedAppId: InstalledAppId,
    jobId: Long,
  ) {
    entry.removeAll { it.installedAppId == installedAppId && it.jobId == jobId }
  }

  suspend fun executeReadyJobs() {
    supervisorScope {
      val now = clock.now()
      val i = entry.iterator()
      while (i.hasNext()) {
        val enqueuedJob = i.next()
        if (enqueuedJob.executeAt != null && enqueuedJob.executeAt > now) continue
        if (handler.execute(enqueuedJob.installedAppId, enqueuedJob.jobId) == null) continue
        i.remove()
      }
    }
  }

  private class Entry(
    val installedAppId: InstalledAppId,
    val jobId: Long,
    val executeAt: Instant?,
  )
}

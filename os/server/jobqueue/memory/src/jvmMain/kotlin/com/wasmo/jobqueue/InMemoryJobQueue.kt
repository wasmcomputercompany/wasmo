package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.supervisorScope
import okio.ByteString

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
    queueName: String,
    job: ByteString,
    executeAt: Instant?,
  ) {
    cancel(installedAppId, queueName, job)
    entry += Entry(
      installedAppId = installedAppId,
      queueName = queueName,
      job = job,
      executeAt = executeAt,
    )
  }

  override fun cancel(
    installedAppId: InstalledAppId,
    queueName: String,
    job: ByteString,
  ) {
    entry.removeAll {
      it.installedAppId == installedAppId && it.queueName == queueName && it.job == job
    }
  }

  suspend fun executeReadyJobs() {
    supervisorScope {
      val now = clock.now()
      val i = entry.iterator()
      while (i.hasNext()) {
        val entry = i.next()
        if (entry.executeAt != null && entry.executeAt > now) continue
        if (handler.execute(entry.installedAppId, entry.queueName, entry.job) == null) continue
        i.remove()
      }
    }
  }

  private class Entry(
    val installedAppId: InstalledAppId,
    val queueName: String,
    val job: ByteString,
    val executeAt: Instant?,
  )
}

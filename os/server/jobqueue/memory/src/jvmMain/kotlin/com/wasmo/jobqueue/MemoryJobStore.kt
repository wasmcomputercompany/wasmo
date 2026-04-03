package com.wasmo.jobqueue

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
  private val jobHandlerMap: Map<HandlerId, JobStore.Handler<*>>,
) : JobStore {
  private val entry = LinkedBlockingQueue<Entry>()

  override fun enqueue(job: Job, executeAt: Instant?) {
    cancel(job)
    entry += Entry(job, executeAt)
  }

  override fun cancel(job: Job) {
    entry.removeAll { it.job == job }
  }

  suspend fun executeReadyJobs() {
    supervisorScope {
      val now = clock.now()
      val i = entry.iterator()
      while (i.hasNext()) {
        val entry = i.next()
        if (entry.executeAt != null && entry.executeAt > now) continue

        val handler = jobHandlerMap[entry.job.handlerId] ?: continue
        if ((handler as JobStore.Handler<Job>).execute(entry.job) == null) continue
        i.remove()
      }
    }
  }

  private class Entry(
    val job: Job,
    val executeAt: Instant?,
  )
}

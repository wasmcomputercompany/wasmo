package com.wasmo.jobqueue

import com.wasmo.api.WasmoJson
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job as CoroutinesJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer

/**
 * A simple job queue.
 */
@Inject
@SingleIn(AppScope::class)
class MemoryJobStore(
  private val scope: CoroutineScope,
  private val clock: Clock,
  private val jobHandlerMap: Map<HandlerId<*>, JobStore.Handler<*>>,
  private val eventListener: JobQueueEventListener,
) : JobStore {
  private val jobs = ConcurrentHashMap<Job, CoroutinesJob>()

  override fun enqueue(job: Job, executeAt: Instant?) {
    eventListener.jobEnqueued(executeAt)

    cancel(job)

    val serializer = job.handlerId.serializer as KSerializer<Job>
    val encodedJob = WasmoJson.encodeToString(serializer, job)

    jobs[job] = enqueueEncoded(serializer, encodedJob, executeAt)
  }

  override fun cancel(job: Job) {
    jobs.remove(job)?.cancel()
  }

  private fun enqueueEncoded(
    serializer: KSerializer<Job>,
    encodedJob: String,
    executeAt: Instant?,
  ): CoroutinesJob {
    return scope.launch {
      if (executeAt != null) {
        val duration = executeAt - clock.now()
        delay(duration)
      }

      val job = WasmoJson.decodeFromString(serializer, encodedJob)
      try {
        val handler = jobHandlerMap[job.handlerId] as JobStore.Handler<Job>?
        handler?.execute(job)
      } catch (e: Throwable) {
        e.printStackTrace() // TODO.
      }
      eventListener.jobCompleted()
    }
  }
}

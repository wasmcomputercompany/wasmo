package com.wasmo.jobs

import com.wasmo.api.WasmoJson
import com.wasmo.events.EventListener
import com.wasmo.identifiers.Job
import com.wasmo.identifiers.JobHandlerId
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job as CoroutinesJob
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import wasmo.sql.SqlConnection

/**
 * A simple job queue that persists nothing.
 */
@Inject
@SingleIn(OsScope::class)
@Suppress("UNCHECKED_CAST") // The handler map is type-keyed so we need unchecked casts.
class MemoryOsJobQueue(
  private val scope: CoroutineScope,
  private val jobHandlerMap: Map<JobHandlerId<*>, OsJobHandler<*>>,
  private val eventListener: EventListener,
) : OsJobQueue {
  private val jobs = ConcurrentHashMap<Job, CoroutinesJob>()

  context(sqlConnection: SqlConnection)
  override suspend fun enqueue(job: Job) {
    eventListener.onEvent(JobEnqueuedEvent)

    cancel(job)

    val serializer = job.handlerId.serializer as KSerializer<Job>
    val encodedJob = WasmoJson.encodeToString(serializer, job)

    jobs[job] = enqueueEncoded(serializer, encodedJob)
  }

  context(sqlConnection: SqlConnection)
  override suspend fun cancel(job: Job) {
    jobs.remove(job)?.cancel()
  }

  private fun enqueueEncoded(
    serializer: KSerializer<Job>,
    encodedJob: String,
  ): CoroutinesJob {
    return scope.launch {
      val job = WasmoJson.decodeFromString(serializer, encodedJob)
      try {
        val handler = jobHandlerMap[job.handlerId] as OsJobHandler<Job>?
        handler?.execute(job)
      } catch (e: Throwable) {
        e.printStackTrace() // TODO.
      }
      eventListener.onEvent(JobCompletedEvent)
    }
  }
}

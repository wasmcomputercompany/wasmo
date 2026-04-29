package com.wasmo.jobs

import com.wasmo.api.WasmoJson
import com.wasmo.events.EventListener
import com.wasmo.identifiers.JobName
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job as CoroutinesJob
import kotlinx.coroutines.launch
import wasmo.sql.SqlConnection

/**
 * A simple job queue that persists nothing.
 */
@Inject
@SingleIn(OsScope::class)
@Suppress("UNCHECKED_CAST") // The handler map is type-keyed so we need unchecked casts.
class MemoryOsJobQueue(
  private val scope: CoroutineScope,
  private val jobHandlerMap: Map<JobName<*, *>, OsJobHandler<*>>,
  private val eventListener: EventListener,
) : OsJobQueue {
  private val jobs = ConcurrentHashMap<Any, CoroutinesJob>()

  context(sqlConnection: SqlConnection)
  override suspend fun <P : Any, R : Any> enqueue(
    jobName: JobName<P, R>,
    job: P,
  ) {
    eventListener.onEvent(JobEnqueuedEvent)

    cancel(jobName, job)

    val serializer = jobName.paramsSerializer
    val encodedJob = WasmoJson.encodeToString(serializer, job)

    jobs[job] = enqueueEncoded(jobName, encodedJob)
  }

  context(sqlConnection: SqlConnection)
  override suspend fun <P : Any, R : Any> cancel(
    jobName: JobName<P, R>,
    job: P,
  ) {
    jobs.remove(job)?.cancel()
  }

  private fun <P : Any, R : Any> enqueueEncoded(
    jobName: JobName<P, R>,
    encodedJob: String,
  ): CoroutinesJob {
    return scope.launch {
      val job = WasmoJson.decodeFromString(jobName.paramsSerializer, encodedJob)
      try {
        val handler = jobHandlerMap[jobName] as OsJobHandler<P>?
        handler?.execute(job)
      } catch (e: Throwable) {
        e.printStackTrace() // TODO.
      }
      eventListener.onEvent(JobCompletedEvent)
    }
  }
}

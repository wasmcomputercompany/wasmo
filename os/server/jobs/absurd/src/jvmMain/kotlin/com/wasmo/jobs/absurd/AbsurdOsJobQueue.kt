@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.jobs.absurd

import com.wasmo.events.EventListener
import com.wasmo.identifiers.JobName
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobCompletedEvent
import com.wasmo.jobs.JobEnqueuedEvent
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import wasmo.sql.SqlConnection

/**
 * Bridge Wasmo's opinionated job queue API to Absurd's slightly-differently-opinionated task queue
 * API.
 *
 * The main difference is that Wasmo requires callers have a [SqlConnection] context to enqueue,
 * whereas Absurd makes that optional.
 */
@Inject
@SingleIn(OsScope::class)
class AbsurdOsJobQueue(
  private val scope: CoroutineScope,
  private val absurdService: AbsurdService,
  private val eventListener: EventListener,
) : OsJobQueue {
  context(sqlConnection: SqlConnection)
  override suspend fun <P : Any, R : Any> enqueue(
    jobName: JobName<P, R>,
    job: P,
  ) {
    eventListener.onEvent(JobEnqueuedEvent)
    absurdService.absurd.spawn(jobName.toAbsurd(), job)

    // TODO: in production, have continuous workers
    // TODO: in test, launch runners when the current transaction commits.
    scope.launch {
      absurdService.absurd.executeBatch("AbsurdOsJobQueue")
      // TODO: don't emit a completed event until the job itself completes successfully.
      eventListener.onEvent(JobCompletedEvent)
    }
  }

  context(sqlConnection: SqlConnection)
  override suspend fun <P : Any, R : Any> cancel(
    jobName: JobName<P, R>,
    job: P,
  ) {
    TODO()
  }
}

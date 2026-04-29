@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.jobs.absurd

import com.wasmo.events.EventListener
import com.wasmo.identifiers.JobName
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobEnqueuedEvent
import com.wasmo.jobs.JobRegistration
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import wasmo.sql.SqlConnection
import wasmox.sql.SqlTransaction

/**
 * Bridge Wasmo's opinionated job queue API to Absurd's slightly-differently-opinionated task queue
 * API.
 *
 * The main difference is that Wasmo requires callers have a [SqlConnection] context to enqueue,
 * whereas Absurd makes that optional.
 */
class AbsurdOsJobQueue<P : Any, R : Any> private constructor(
  private val factory: Factory,
  private val jobName: JobName<P, R>,
) : OsJobQueue<P> {

  context(sqlTransaction: SqlTransaction)
  override suspend fun enqueue(job: P) {
    factory.eventListener.onEvent(JobEnqueuedEvent)
    factory.absurdService.absurd.spawn(
      taskName = jobName.toAbsurd(),
      params = job,
    )

    // TODO: in production, have continuous workers
    sqlTransaction.afterCommit {
      factory.scope.launch {
        factory.absurdService.absurd.executeBatch("AbsurdOsJobQueue")
      }
    }
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun cancel(job: P) {
    TODO()
  }

  @Inject
  @SingleIn(OsScope::class)
  class Factory(
    internal val scope: CoroutineScope,
    internal val absurdService: AbsurdService,
    internal val eventListener: EventListener,
  ) : OsJobQueue.Factory {
    override fun <P : Any> create(jobName: JobName<P, *>) = AbsurdOsJobQueue(this, jobName)

    override operator fun plus(registration: JobRegistration<*, *>) = Factory(
      scope = scope,
      absurdService = absurdService + registration,
      eventListener = eventListener,
    )
  }
}

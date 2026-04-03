package com.wasmo.jobqueue

import com.wasmo.identifiers.Job
import kotlin.time.Instant

interface JobStore {
  fun enqueue(job: Job, executeAt: Instant? = null)
  fun cancel(job: Job)

  interface Handler<J : Job> {
    suspend fun execute(job: J)
  }
}

interface JobQueueEventListener {
  fun jobEnqueued(instant: Instant?)
  fun jobCompleted()

  companion object {
    val None = object : JobQueueEventListener {
      override fun jobEnqueued(instant: Instant?) {
      }

      override fun jobCompleted() {
      }
    }
  }
}

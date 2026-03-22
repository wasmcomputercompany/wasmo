package com.wasmo.jobs

import app.cash.sqldelight.TransactionCallbacks
import kotlin.time.Instant

/**
 * Executes jobs, possibly in a different process.
 */
interface JobQueue<T> {
  context(transactionCallbacks: TransactionCallbacks)
  fun enqueue(job: T, instant: Instant? = null)
}

interface JobExecutor<T> {
  suspend fun execute(job: T)
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

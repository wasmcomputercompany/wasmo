package com.wasmo.jobs

import app.cash.sqldelight.TransactionCallbacks
import kotlin.time.Instant

interface JobQueue<T> {
  context(transactionCallbacks: TransactionCallbacks)
  fun enqueue(job: T, instant: Instant? = null)
}

interface JobExecutor<T> {
  suspend fun execute(job: T)
}

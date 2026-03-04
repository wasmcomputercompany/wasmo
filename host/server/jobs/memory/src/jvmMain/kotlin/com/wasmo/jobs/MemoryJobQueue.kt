package com.wasmo.jobs

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.WasmoJson
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer

/**
 * This job queue is inappropriate for production use.
 */
class MemoryJobQueue<T>(
  private val clock: Clock,
  private val serializer: KSerializer<T>,
  private val executor: JobExecutor<T>,
  private val scope: CoroutineScope,
) : JobQueue<T> {
  context(transactionCallbacks: TransactionCallbacks)
  override fun enqueue(job: T, instant: Instant?) {
    enqueueEncoded(WasmoJson.encodeToString(serializer, job), instant)
  }

  context(transactionCallbacks: TransactionCallbacks)
  private fun enqueueEncoded(encodedJob: String, instant: Instant?) {
    transactionCallbacks.afterCommit {
      scope.launch {
        if (instant != null) {
          delay(instant - clock.now())
        }

        val job = WasmoJson.decodeFromString(serializer, encodedJob)
        executor.execute(job)
      }
    }
  }
}

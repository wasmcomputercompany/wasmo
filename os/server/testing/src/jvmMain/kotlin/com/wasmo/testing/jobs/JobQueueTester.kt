package com.wasmo.testing.jobs

import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobQueueEventListener
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@Inject
@SingleIn(OsScope::class)
class JobQueueTester : JobQueueEventListener {
  private val jobCount = MutableStateFlow(0)

  override fun jobEnqueued(instant: Instant?) {
    jobCount.value++
  }

  override fun jobCompleted() {
    jobCount.value--
  }

  suspend fun awaitIdle() {
    jobCount.first { it == 0 }
  }
}

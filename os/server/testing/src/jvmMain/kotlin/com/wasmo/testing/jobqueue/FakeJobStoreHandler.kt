package com.wasmo.testing.jobqueue

import com.wasmo.jobqueue.Job
import com.wasmo.jobqueue.JobStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class FakeJobStoreHandler : JobStore.Handler<Job> {
  val executedJobs = Channel<Job>(capacity = Int.MAX_VALUE)
  val jobResults = Channel<Result<Unit>>(capacity = Int.MAX_VALUE)

  context(scope: CoroutineScope)
  override suspend fun execute(job: Job) = scope.launch {
    executedJobs.send(job)
    jobResults.receive().getOrThrow()
  }
}

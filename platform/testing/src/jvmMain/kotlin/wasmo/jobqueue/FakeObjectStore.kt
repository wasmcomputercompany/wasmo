package wasmo.jobqueue

import kotlin.time.Instant
import wasmo.jobs.JobQueue

class FakeJobQueue : JobQueue {
  override fun enqueue(jobId: Long, executeAt: Instant?) {
    TODO()
  }

  override fun cancel(jobId: Long) {
    TODO()
  }
}

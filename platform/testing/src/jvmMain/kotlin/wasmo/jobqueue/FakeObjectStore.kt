package wasmo.jobqueue

import kotlin.time.Instant
import okio.ByteString
import wasmo.jobs.JobQueue

class FakeJobQueueFactory : JobQueue.Factory {
  suspend fun awaitIdle() {
  }

  override fun get(name: String) = FakeJobQueue(name)

  class FakeJobQueue internal constructor(
    private val queueName: String,
  ) : JobQueue {
    override fun enqueue(job: ByteString, executeAt: Instant?) {
      TODO()
    }

    override fun cancel(job: ByteString) {
      TODO()
    }
  }
}

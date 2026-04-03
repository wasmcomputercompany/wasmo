package wasmo.jobqueue

import kotlin.time.Instant
import okio.ByteString
import wasmo.jobs.JobQueue

class FakeJobQueue private constructor(
  private val queueName: String,
) : JobQueue {
  override fun enqueue(job: ByteString, executeAt: Instant?) {
    TODO()
  }

  override fun cancel(job: ByteString) {
    TODO()
  }

  class Factory : JobQueue.Factory {
    override fun get(name: String) = FakeJobQueue(
      queueName = name,
    )
  }
}

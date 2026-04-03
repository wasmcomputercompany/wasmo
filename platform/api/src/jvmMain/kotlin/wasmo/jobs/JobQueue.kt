package wasmo.jobs

import kotlin.time.Instant
import okio.ByteString

interface JobQueue {
  /**
   * Enqueues a job to be executed either immediately (if [executeAt] is null), or at the specified
   * time.
   *
   * Enqueuing a job ID that's already enqueued is the same as canceling that job ID and then
   * enqueueing it.
   *
   * @param job an opaque value that will be passed back to the handler.
   * @param executeAt the time when the OS will attempt to run the job. Actual execution may be
   *   delayed due to availability of resources.
   */
  fun enqueue(
    job: ByteString,
    executeAt: Instant?,
  )

  /**
   * Attempts to cancel [job] from executing. This has no effect if the job has already started
   * executing.
   */
  fun cancel(job: ByteString)

  interface Factory {
    /** Get the named job queue. Use `""` for the application's default job queue. */
    fun get(name: String = ""): JobQueue
  }
}

interface JobHandler {
  suspend fun handle(job: ByteString)
  suspend fun handleFailed(job: ByteString)

  interface Factory {
    fun get(queueName: String): JobHandler
  }
}

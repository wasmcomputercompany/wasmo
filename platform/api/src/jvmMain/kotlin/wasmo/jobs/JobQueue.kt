package wasmo.jobs

import kotlin.time.Instant

interface JobQueue {
  /**
   * Enqueues a job to be executed either immediately (if [executeAt] is null), or at the specified
   * time.
   *
   * Enqueuing a job ID that's already enqueued is the same as canceling that job ID and then
   * enqueueing it.
   *
   * @param jobId an opaque identifier that will be passed back to the handler.
   * @param executeAt the time when the OS will attempt to run the job. Actual execution may be
   *   delayed due to availability of resources.
   */
  fun enqueue(
    jobId: Long,
    executeAt: Instant?,
  )

  /**
   * Attempts to cancel [jobId] from executing. This has no effect if the job has already started
   * executing.
   */
  fun cancel(jobId: Long)
}

interface JobHandler {
  fun handle(jobId: Long)
  fun handleFailedJob(jobId: Long)
}

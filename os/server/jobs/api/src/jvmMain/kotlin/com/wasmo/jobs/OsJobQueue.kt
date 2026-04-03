package com.wasmo.jobs

import com.wasmo.identifiers.Job
import kotlin.time.Instant

/**
 * A job queue scoped to the entire OS.
 *
 * Unlike the app job queue, this can do strongly-typed jobs for the OS's internal use.
 */
interface OsJobQueue {
  fun enqueue(job: Job, executeAt: Instant? = null)
  fun cancel(job: Job)
}

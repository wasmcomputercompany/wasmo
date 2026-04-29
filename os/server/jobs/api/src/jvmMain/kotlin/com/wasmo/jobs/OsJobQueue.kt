package com.wasmo.jobs

import com.wasmo.identifiers.Job
import wasmo.sql.SqlConnection

/**
 * A job queue scoped to the entire OS.
 *
 * Unlike the app job queue, this can do strongly-typed jobs for the OS's internal use.
 */
interface OsJobQueue {
  context(sqlConnection: SqlConnection)
  suspend fun enqueue(job: Job)

  context(sqlConnection: SqlConnection)
  suspend fun cancel(job: Job)
}

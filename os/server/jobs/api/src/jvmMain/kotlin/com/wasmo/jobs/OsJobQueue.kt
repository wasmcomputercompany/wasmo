package com.wasmo.jobs

import com.wasmo.identifiers.JobName
import wasmox.sql.SqlTransaction

/**
 * A job queue scoped to the entire OS.
 *
 * Unlike the app job queue, this can do strongly-typed jobs for the OS's internal use.
 */
interface OsJobQueue {
  context(sqlTransaction: SqlTransaction)
  suspend fun <P : Any, R : Any> enqueue(jobName: JobName<P, R>, job: P)

  context(sqlTransaction: SqlTransaction)
  suspend fun <P : Any, R : Any> cancel(jobName: JobName<P, R>, job: P)
}

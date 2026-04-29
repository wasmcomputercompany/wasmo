package com.wasmo.jobs

import com.wasmo.identifiers.JobName
import wasmox.sql.SqlTransaction

/**
 * A job queue scoped to the entire OS.
 *
 * Unlike the app job queue, this can do strongly-typed jobs for the OS's internal use.
 */
interface OsJobQueue<P : Any> {
  context(sqlTransaction: SqlTransaction)
  suspend fun enqueue(job: P)

  context(sqlTransaction: SqlTransaction)
  suspend fun cancel(job: P)

  interface Factory {
    fun <P : Any> create(jobName: JobName<P, *>): OsJobQueue<P>
    operator fun plus(registration: JobRegistration<*, *>): Factory
  }
}

package com.wasmo.jobs

interface OsJobHandler<P : Any> {
  suspend fun execute(job: P)
}

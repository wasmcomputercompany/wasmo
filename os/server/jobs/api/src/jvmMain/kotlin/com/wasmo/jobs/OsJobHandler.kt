package com.wasmo.jobs

import com.wasmo.identifiers.Job

interface OsJobHandler<J : Job> {
  suspend fun execute(job: J)
}

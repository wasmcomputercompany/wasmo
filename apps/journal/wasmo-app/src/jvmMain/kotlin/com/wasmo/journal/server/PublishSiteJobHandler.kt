package com.wasmo.journal.server

import okio.ByteString
import wasmo.jobs.JobHandler

class PublishSiteJobHandler : JobHandler {
  override suspend fun handle(job: ByteString) {

  }

  companion object {
    val QueueName = "publish-site"
  }
}

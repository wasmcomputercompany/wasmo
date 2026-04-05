package com.wasmo.journal.server

import okio.ByteString
import wasmo.jobs.JobHandler

class PublishSiteJobHandler(
  private val sitePublisher: SitePublisher,
) : JobHandler {
  override suspend fun handle(job: ByteString) {
    sitePublisher.publishSite()
  }

  companion object {
    val QueueName = "publish-site"
  }
}

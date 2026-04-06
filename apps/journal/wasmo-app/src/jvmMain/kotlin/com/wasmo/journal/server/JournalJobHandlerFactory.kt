package com.wasmo.journal.server

import com.wasmo.journal.server.publishing.SitePublisher
import wasmo.jobs.JobHandler

class JournalJobHandlerFactory(
  val sitePublisher: SitePublisher,
) : JobHandler.Factory {
  override fun get(queueName: String): JobHandler {
    return when (queueName) {
      SitePublisher.QueueName -> sitePublisher

      else -> error("unexpected queue name: $queueName")
    }
  }
}

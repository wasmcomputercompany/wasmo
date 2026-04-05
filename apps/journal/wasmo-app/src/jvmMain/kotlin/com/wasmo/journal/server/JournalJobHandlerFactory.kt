package com.wasmo.journal.server

import wasmo.jobs.JobHandler

class JournalJobHandlerFactory(
  val publishSiteJobHandler: PublishSiteJobHandler,
) : JobHandler.Factory {
  override fun get(queueName: String): JobHandler {
    return when (queueName) {
      PublishSiteJobHandler.QueueName -> publishSiteJobHandler

      else -> error("unexpected queue name: $queueName")
    }
  }
}

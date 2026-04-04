package com.wasmo.journal.server

import wasmo.jobs.JobHandler

class JournalJobHandlerFactory : JobHandler.Factory {
  override fun get(queueName: String): JobHandler {
    return when (queueName) {
      PublishSiteJobHandler.QueueName -> PublishSiteJobHandler()
      else -> error("unexpected queue name: $queueName")
    }
  }
}

package com.wasmo.journal.server.publishing

import com.wasmo.journal.api.PublishState
import com.wasmo.journal.api.RequestPublishRequest
import okio.ByteString
import wasmo.jobs.JobQueue

/**
 * ```
 * POST /api/request-publish
 * ```
 */
class RequestPublishAction(
  private val publishSiteJobQueue: JobQueue,
  private val publishTracker: PublishTracker,
) {
  suspend fun requestPublish(request: RequestPublishRequest): PublishState {
    publishSiteJobQueue.enqueue(ByteString.EMPTY)
    return publishTracker.getPublishState()
  }

  companion object {
    val PathRegex = Regex("/api/request-publish")
  }
}

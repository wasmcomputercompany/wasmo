package com.wasmo.journal.server.admin

import com.wasmo.journal.api.RequestPublishRequest
import com.wasmo.journal.api.RequestPublishResponse
import okio.ByteString
import wasmo.jobs.JobQueue

/**
 * ```
 * POST /api/request-publish
 * ```
 */
class RequestPublishAction(
  private val publishSiteJobQueue: JobQueue,
) {
  suspend fun requestPublish(request: RequestPublishRequest): RequestPublishResponse {
    publishSiteJobQueue.enqueue(ByteString.EMPTY)
    return RequestPublishResponse
  }

  companion object {
    val PathRegex = Regex("/api/request-publish")
  }
}

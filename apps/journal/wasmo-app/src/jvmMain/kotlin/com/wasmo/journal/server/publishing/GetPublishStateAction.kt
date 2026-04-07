package com.wasmo.journal.server.publishing

/**
 * ```
 * GET /api/publish-state
 * ```
 */
class GetPublishStateAction(
  private val publishTracker: PublishTracker,
) {
  suspend fun get() = publishTracker.getPublishState()

  suspend fun get(
    match: MatchResult,
  ) = get()

  companion object {
    val PathRegex = Regex("/api/publish-state")
  }
}

package com.wasmo.journal.server.publishing

import app.cash.sqldelight.async.coroutines.awaitAsOne
import com.wasmo.journal.api.PublishState
import com.wasmo.journal.db.JournalDb
import kotlin.time.Clock
import kotlin.time.Instant

class PublishTracker(
  val clock: Clock,
  val journalDb: JournalDb,
) {
  /** Insert the single row in the `PublishState` table. */
  suspend fun migrate(oldVersion: Long, newVersion: Long) {
    val now = clock.now()
    if (oldVersion == 0L) {
      journalDb.publishStateQueries.initializePublishStateA()
      journalDb.publishStateQueries.initializePublishStateB(
        version = 1L,
        publish_needed_at = null,
        last_published_at = now,
      )
    }
  }

  suspend fun setPublishNeeded(now: Instant) {
    val oldPublishState = journalDb.publishStateQueries.findPublishState()
      .awaitAsOne()
    if (oldPublishState.publish_needed_at != null) return // Already needed.

    val rowCount = journalDb.publishStateQueries.setPublishNeededAt(
      new_version = oldPublishState.version + 1L,
      publish_needed_at = now,
      expected_version = oldPublishState.version,
    )
    check(rowCount == 1L)
  }

  suspend fun getPublishState(): PublishState {
    val publishState = journalDb.publishStateQueries.findPublishState()
      .awaitAsOne()
    return PublishState(
      publishNeededAt = publishState.publish_needed_at,
      lastPublishedAt = publishState.last_published_at,
    )
  }
}

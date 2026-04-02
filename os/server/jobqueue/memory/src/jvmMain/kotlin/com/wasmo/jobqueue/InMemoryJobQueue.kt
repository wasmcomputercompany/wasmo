package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope

@Inject
@SingleIn(AppScope::class)
class InMemoryJobStore(
  val scope: CoroutineScope,
) : JobStore {
  val jobs = mutableListOf<EnqueuedJob>()

  override fun enqueue(installedAppId: InstalledAppId, jobId: Long, executeAt: Instant?) {
    jobs += EnqueuedJob(
      installedAppId,
      jobId,
      executeAt,
    )
  }

  override fun cancel(installedAppId: InstalledAppId, jobId: Long) {
  }
}

class EnqueuedJob(
  val installedAppId: InstalledAppId,
  val jobId: Long,
  val executeAt: Instant?,
)

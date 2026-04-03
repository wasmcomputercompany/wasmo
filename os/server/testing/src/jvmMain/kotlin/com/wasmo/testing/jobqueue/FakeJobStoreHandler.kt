package com.wasmo.testing.jobqueue

import com.wasmo.identifiers.InstalledAppId
import com.wasmo.jobqueue.JobStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okio.ByteString

class FakeJobStoreHandler : JobStore.Handler {
  val executedJobs = Channel<Entry>(capacity = Int.MAX_VALUE)
  val jobResults = Channel<Result<Unit>>(capacity = Int.MAX_VALUE)

  context(scope: CoroutineScope)
  override suspend fun execute(
    installedAppId: InstalledAppId,
    queueName: String,
    job: ByteString,
  ) = scope.launch {
    executedJobs.send(Entry(installedAppId, queueName, job))
    jobResults.receive().getOrThrow()
  }

  data class Entry(
    val installedAppId: InstalledAppId,
    val queueName: String,
    val job: ByteString,
  )
}

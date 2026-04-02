package com.wasmo.jobqueue

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.InstalledAppId
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import wasmo.time.FakeClock

class MemoryJobStoreTest {
  private val clock = FakeClock()
  private val handler = FakeHandler()

  @Test
  fun happyPath() = runTest {
    val jobStore = MemoryJobStore(clock, handler)

    jobStore.enqueue(InstalledAppId(1L), 2L, null)

    launch {
      jobStore.executeReadyJobs()
    }

    assertThat(handler.executedJobs.receive())
      .isEqualTo(InstalledAppId(1L) to 2L)
    handler.jobResults.send(Result.success(Unit))
  }

  class FakeHandler : JobStore.Handler {
    val executedJobs = Channel<Pair<InstalledAppId, Long>>(capacity = Int.MAX_VALUE)
    val jobResults = Channel<Result<Unit>>(capacity = Int.MAX_VALUE)

    context(scope: CoroutineScope)
    override suspend fun execute(
      installedAppId: InstalledAppId,
      jobId: Long,
    ) = scope.launch {
      executedJobs.send(installedAppId to jobId)
      jobResults.receive().getOrThrow()
    }
  }
}

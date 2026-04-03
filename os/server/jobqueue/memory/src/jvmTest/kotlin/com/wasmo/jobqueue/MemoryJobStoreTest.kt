package com.wasmo.jobqueue

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.jobqueue.Job.ApplicationJob
import com.wasmo.testing.jobqueue.FakeJobStoreHandler
import kotlin.test.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.time.FakeClock

class MemoryJobStoreTest {
  private val clock = FakeClock()
  private val handler = FakeJobStoreHandler()

  @Test
  fun happyPath() = runTest {
    val jobStore = MemoryJobStore(
      clock,
      mapOf(HandlerId.Application to handler),
    )

    val job = ApplicationJob(InstalledAppId(1L), "emails", "2".encodeUtf8())
    jobStore.enqueue(job)

    launch {
      jobStore.executeReadyJobs()
    }

    assertThat(handler.executedJobs.receive()).isEqualTo(job)
    handler.jobResults.send(Result.success(Unit))
  }
}

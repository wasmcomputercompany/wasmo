package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class InMemoryJobStoreTest {
  @Test
  fun happyPath() = runTest {
    val jobStore = InMemoryJobStore(this)

    jobStore.enqueue(InstalledAppId(1L), 2L, null)
    jobStore.cancel(InstalledAppId(1L), 2L)
  }
}

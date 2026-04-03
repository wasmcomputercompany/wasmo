package com.wasmo.jobs

import kotlin.time.Instant

interface JobQueueEventListener {
  fun jobEnqueued(instant: Instant?)
  fun jobCompleted()

  companion object {
    val None = object : JobQueueEventListener {
      override fun jobEnqueued(instant: Instant?) {
      }

      override fun jobCompleted() {
      }
    }
  }
}

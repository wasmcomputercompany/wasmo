package com.wasmo.jobs

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface JobProcessor {
  fun start(
    workerCount: Int = 1,
    batchSize: Int = 10,
    idleDelay: Duration = 250.milliseconds,
  )
}

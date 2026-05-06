package com.wasmo.jobs.absurd

import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobProcessor
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Inject
@SingleIn(OsScope::class)
internal class AbsurdOsJobProcessor(
  private val scope: CoroutineScope,
  private val absurdService: AbsurdService,
) : JobProcessor {
  override fun start(
    workerCount: Int,
    batchSize: Int,
    idleDelay: Duration,
  ) {
    for (i in 0 until workerCount) {
      scope.launch {
        while (scope.isActive) {
          val executedTaskCount = absurdService.absurd.executeBatch(
            workerId = "AbsurdOsJobProcessor-$i",
            batchSize = batchSize,
          )
          if (executedTaskCount < batchSize) {
            delay(idleDelay)
          }
        }
      }
    }
  }
}

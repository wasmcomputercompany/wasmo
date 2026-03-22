package com.wasmo.testing

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Like [kotlin.time.measureTime], but using the test coroutine scheduler's clock.
 */
suspend fun CoroutineScope.measureTestTime(block: suspend () -> Unit): Duration {
  val scheduler = coroutineContext[TestCoroutineScheduler.Key]!!
  val startMilliseconds = scheduler.currentTime
  block()
  return (scheduler.currentTime - startMilliseconds).milliseconds
}

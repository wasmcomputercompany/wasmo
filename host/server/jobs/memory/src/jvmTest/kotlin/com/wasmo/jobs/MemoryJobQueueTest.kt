package com.wasmo.jobs

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class MemoryJobQueueTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val channel = Channel<String>(capacity = 1)
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executorLazy = lazyOf(SampleJobExecutor(channel)),
      scope = this,
      eventListener = tester.jobQueueTester,
    )

    tester.wasmoDb.transactionWithResult(noEnclosing = true) {
      jobQueue.enqueue(SampleJob("hello"))
    }

    val elapsed = measureTestTime {
      assertThat(channel.receive()).isEqualTo("hello")
    }
    assertThat(elapsed).isEqualTo(Duration.ZERO)
  }

  @Test
  fun jobExecutedWithDelay() = runTest {
    val channel = Channel<String>(capacity = 1)
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executorLazy = lazyOf(SampleJobExecutor(channel)),
      scope = this,
      eventListener = tester.jobQueueTester,
    )

    tester.wasmoDb.transactionWithResult(noEnclosing = true) {
      jobQueue.enqueue(SampleJob("hello"), tester.clock.now.plus(1.minutes))
    }

    val elapsed = measureTestTime {
      assertThat(channel.receive()).isEqualTo("hello")
    }
    assertThat(elapsed).isEqualTo(1.minutes)
  }

  @Test
  fun jobNotExecutedWhenTransactionCanceled() = runTest {
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executorLazy = lazy { error("unexpected call") },
      scope = this,
      eventListener = tester.jobQueueTester,
    )

    assertFailsWith<IllegalStateException> {
      tester.wasmoDb.transactionWithResult(noEnclosing = true) {
        jobQueue.enqueue(SampleJob("hello"))
        throw IllegalStateException("boom")
      }
    }
  }

  @Test
  fun awaitIdleAlreadyIdle() = runTest {
    val channel = Channel<String>(capacity = Channel.RENDEZVOUS)
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executorLazy = lazyOf(SampleJobExecutor(channel)),
      scope = this,
      eventListener = tester.jobQueueTester,
    )

    tester.wasmoDb.transactionWithResult(noEnclosing = true) {
      jobQueue.enqueue(SampleJob("hello"))
    }

    delay(3.seconds)
    assertThat(channel.receive()).isEqualTo("hello")

    val durationDeferred = async {
      measureTestTime {
        tester.jobQueueTester.awaitIdle()
      }
    }

    assertThat(durationDeferred.await()).isEqualTo(Duration.ZERO)
  }

  @Test
  fun awaitIdleNeedsToWait() = runTest {
    val channel = Channel<String>(capacity = Channel.RENDEZVOUS)
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executorLazy = lazyOf(SampleJobExecutor(channel)),
      scope = this,
      eventListener = tester.jobQueueTester,
    )

    tester.wasmoDb.transactionWithResult(noEnclosing = true) {
      jobQueue.enqueue(SampleJob("hello"))
    }

    val durationDeferred = async {
      measureTestTime {
        tester.jobQueueTester.awaitIdle()
      }
    }

    delay(3.seconds)
    assertThat(channel.receive()).isEqualTo("hello")

    assertThat(durationDeferred.await()).isEqualTo(3.seconds)
  }

  @Serializable
  data class SampleJob(
    val message: String,
  )

  class SampleJobExecutor(
    val channel: Channel<String>,
  ) : JobExecutor<SampleJob> {
    override suspend fun execute(job: SampleJob) {
      channel.send(job.message)
    }
  }

  private suspend fun CoroutineScope.measureTestTime(block: suspend () -> Unit): Duration {
    val scheduler = coroutineContext[TestCoroutineScheduler.Key]!!
    val startMilliseconds = scheduler.currentTime
    block()
    return (scheduler.currentTime - startMilliseconds).milliseconds
  }
}

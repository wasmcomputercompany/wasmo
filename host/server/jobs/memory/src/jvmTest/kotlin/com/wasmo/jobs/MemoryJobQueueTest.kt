package com.wasmo.jobs

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.ServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable

class MemoryJobQueueTest {
  lateinit var tester: ServiceTester

  @BeforeTest
  fun setUp() {
    tester = ServiceTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() = runTest {
    val channel = Channel<String>(capacity = 1)
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executor = SampleJobExecutor(channel),
      scope = this,
    )

    tester.wasmoDb.transactionWithResult(noEnclosing = true) {
      jobQueue.enqueue(SampleJob("hello"))
    }

    this.testScheduler.currentTime
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
      executor = SampleJobExecutor(channel),
      scope = this,
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
    val channel = Channel<String>(capacity = 1)
    val jobQueue = MemoryJobQueue(
      clock = tester.clock,
      serializer = SampleJob.serializer(),
      executor = SampleJobExecutor(channel),
      scope = this,
    )

    assertFailsWith<IllegalStateException> {
      tester.wasmoDb.transactionWithResult(noEnclosing = true) {
        jobQueue.enqueue(SampleJob("hello"))
        throw IllegalStateException("boom")
      }
    }

    assertFailsWith<TimeoutCancellationException> {
      withTimeout(1.seconds) {
        channel.receive()
      }
    }
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

  private suspend fun TestScope.measureTestTime(block: suspend () -> Unit): Duration {
    val startMilliseconds = testScheduler.currentTime
    block()
    return (testScheduler.currentTime - startMilliseconds).milliseconds
  }
}

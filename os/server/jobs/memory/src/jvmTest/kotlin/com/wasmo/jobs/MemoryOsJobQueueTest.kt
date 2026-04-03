package com.wasmo.jobs

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.HandlerId
import com.wasmo.identifiers.Job
import com.wasmo.testing.measureTestTime
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

class MemoryOsJobQueueTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val channel = Channel<String>(capacity = 1)
    val jobQueue = MemoryOsJobQueue(
      scope = this,
      clock = tester.clock,
      jobHandlerMap = mapOf(SampleJobHandlerId to SampleJobExecutor(channel)),
      eventListener = tester.jobQueueTester,
    )

    jobQueue.enqueue(SampleJob("hello"))

    val elapsed = measureTestTime {
      assertThat(channel.receive()).isEqualTo("hello")
    }
    assertThat(elapsed).isEqualTo(Duration.ZERO)
  }

  @Test
  fun jobExecutedWithDelay() = runTest {
    val channel = Channel<String>(capacity = 1)
    val jobQueue = MemoryOsJobQueue(
      scope = this,
      clock = tester.clock,
      jobHandlerMap = mapOf(SampleJobHandlerId to SampleJobExecutor(channel)),
      eventListener = tester.jobQueueTester,
    )

    jobQueue.enqueue(SampleJob("hello"), tester.clock.now.plus(1.minutes))

    val elapsed = measureTestTime {
      assertThat(channel.receive()).isEqualTo("hello")
    }
    assertThat(elapsed).isEqualTo(1.minutes)
  }

  @Test
  fun jobNotExecutedWhenJobCanceled() = runTest {
    val explodingExecutor = object : OsJobQueue.Handler<SampleJob> {
      override suspend fun execute(job: SampleJob) {
        error("unexpected call")
      }
    }
    val jobQueue = MemoryOsJobQueue(
      scope = this,
      clock = tester.clock,
      jobHandlerMap = mapOf(SampleJobHandlerId to explodingExecutor),
      eventListener = tester.jobQueueTester,
    )

    val job = SampleJob("hello")
    jobQueue.enqueue(job)
    jobQueue.cancel(job)
  }

  @Test
  fun awaitIdleAlreadyIdle() = runTest {
    val channel = Channel<String>(capacity = Channel.RENDEZVOUS)
    val jobQueue = MemoryOsJobQueue(
      scope = this,
      clock = tester.clock,
      jobHandlerMap = mapOf(SampleJobHandlerId to SampleJobExecutor(channel)),
      eventListener = tester.jobQueueTester,
    )

    jobQueue.enqueue(SampleJob("hello"))

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
    val jobQueue = MemoryOsJobQueue(
      scope = this,
      clock = tester.clock,
      jobHandlerMap = mapOf(SampleJobHandlerId to SampleJobExecutor(channel)),
      eventListener = tester.jobQueueTester,
    )

    jobQueue.enqueue(SampleJob("hello"))

    val durationDeferred = async {
      measureTestTime {
        tester.jobQueueTester.awaitIdle()
      }
    }

    delay(3.seconds)
    assertThat(channel.receive()).isEqualTo("hello")

    assertThat(durationDeferred.await()).isEqualTo(3.seconds)
  }

  object SampleJobHandlerId : HandlerId<SampleJob> {
    override val serializer: KSerializer<SampleJob>
      get() = SampleJob.serializer()
  }

  @Serializable
  data class SampleJob(
    val message: String,
  ) : Job {
    override val handlerId: HandlerId<*>
      get() = SampleJobHandlerId
  }

  class SampleJobExecutor(
    val channel: Channel<String>,
  ) : OsJobQueue.Handler<SampleJob> {
    override suspend fun execute(job: SampleJob) {
      channel.send(job.message)
    }
  }
}

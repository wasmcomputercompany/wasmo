package com.wasmo.jobs.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.JobName
import com.wasmo.jobs.JobRegistration
import com.wasmo.jobs.OsJobHandler
import com.wasmo.testing.measureTestTime
import com.wasmo.testing.service.ServiceTester
import kotlin.time.Duration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Test
import wasmox.sql.transaction

class AbsurdOsJobQueueTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val channel = Channel<String>(capacity = 1)

    val jobQueueFactory = tester.jobQueueFactory +
      JobRegistration(SampleJob.JobName, SampleJobHandler(channel))
    val jobQueue = jobQueueFactory.create(SampleJob.JobName)

    tester.wasmoDb.transaction {
      jobQueue.enqueue(SampleJob("hello"))
    }

    val elapsed = measureTestTime {
      assertThat(channel.receive()).isEqualTo("hello")
    }
    assertThat(elapsed).isEqualTo(Duration.ZERO)
  }

  @Serializable
  data class SampleJob(
    val message: String,
  ) {
    companion object {
      val JobName = JobName<SampleJob, Unit>("SampleJob")
    }
  }

  class SampleJobHandler(
    val channel: Channel<String>,
  ) : OsJobHandler<SampleJob, Unit> {
    context(context: OsJobHandler.Context)
    override suspend fun handle(job: SampleJob) {
      channel.send(job.message)
    }
  }
}

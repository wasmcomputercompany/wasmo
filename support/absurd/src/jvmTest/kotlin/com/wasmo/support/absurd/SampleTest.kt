package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class SampleTest {
  @InterceptTest
  val tester = AbsurdTester()

  @Test
  @Ignore("not implemented yet")
  fun sample() = runTest {
    val provisionUser = TaskName<ProvisionUserParams, ProvisionUserResult>("provision-user")
    tester.absurd.registerTask(
      name = provisionUser,
      taskHandler = object : TaskHandler<ProvisionUserParams, ProvisionUserResult> {
        context(context: TaskHandler.Context<ProvisionUserParams, ProvisionUserResult>)
        override fun handle(params: ProvisionUserParams): ProvisionUserResult {
          return ProvisionUserResult(
            params.userId,
            params.email,
            Delivery(
              sent = true,
              provider = "demo-mail",
              to = params.email,
            ),
            status = "active",
            activatedAt = tester.clock.now(),
          )
        }
      },
    )

    val spawnResult = tester.absurd.spawn(
      provisionUser,
      ProvisionUserParams(
        userId = "alice",
        email = "alice@example.com",
      ),
    )

    val taskResult = tester.absurd.fetchTaskResult(
      taskId = spawnResult.taskId,
      taskName = provisionUser,
    )
  }

  @Serializable
  data class ProvisionUserParams(
    val userId: String,
    val email: String,
  )

  @Serializable
  data class Delivery(
    val sent: Boolean,
    val provider: String,
    val to: String,
  )

  @Serializable
  data class ProvisionUserResult(
    val userId: String,
    val email: String,
    val delivery: Delivery,
    val status: String,
    val activatedAt: Instant,
  )
}

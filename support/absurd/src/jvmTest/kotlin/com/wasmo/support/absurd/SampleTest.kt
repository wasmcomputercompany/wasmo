package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

@Serializable
data class ProvisionUserParams(
  val userId: String,
  val email: String,
)

@Serializable
data class UserRecord(
  val userId: String,
  val email: String,
  val createdAt: Instant,
)

@Serializable
data class OutageState(
  val simulated: Boolean,
)

@Serializable
data class DeliveryResult(
  val sent: Boolean,
  val provider: String,
  val to: String,
)

@Serializable
data class ActivationEvent(
  val activatedAt: Instant,
)

@Serializable
data class ProvisionUserResult(
  val userId: String,
  val email: String,
  val delivery: DeliveryResult,
  val status: String,
  val activatedAt: Instant,
)

@OptIn(ExperimentalUuidApi::class)
class SampleTest {
  @InterceptTest
  val tester = AbsurdTester()

  @Test
  fun sample() = runTest {
    val provisionUser = TaskName<ProvisionUserParams, ProvisionUserResult>("provision-user")
    tester.absurd.registerTask(
      name = provisionUser,
      taskHandler = object : TaskHandler<ProvisionUserParams, ProvisionUserResult> {
        context(context: TaskHandler.Context<ProvisionUserParams, ProvisionUserResult>)
        override suspend fun handle(params: ProvisionUserParams): ProvisionUserResult {
          val user = context.step("create-user-record") {
            println("${context.taskId} creating user record for ${params.userId}")
            UserRecord(
              userId = params.userId,
              email = params.email,
              createdAt = tester.clock.now(),
            )
          }

          // Demo only: fail once after the first checkpoint so the retry
          // behavior is visible.
          val outage = context.beginStep<OutageState>("demo-transient-outage")
          if (outage.result == null) {
            println("${context.taskId} simulating a temporary email provider outage")
            outage.complete(OutageState(simulated = true))
            throw Exception("temporary email provider outage")
          }

          val delivery = context.step("send-activation-email") {
            println("${context.taskId} sending activation email to ${user.email}")
            DeliveryResult(
              sent = true,
              provider = "demo-mail",
              to = user.email,
            )
          }

          println("${context.taskId} waiting for user-activated:${user.userId}")

          val activation = context.awaitEvent<ActivationEvent>(
            "user-activated:${user.userId}",
            timeout = 3600.seconds,
          )

          return ProvisionUserResult(
            userId = params.userId,
            email = params.email,
            delivery = delivery,
            status = "active",
            activatedAt = activation.activatedAt,
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

    assertThat(taskResult)
      .isNotNull()
      .isInstanceOf<TaskResult.Pending<*, *>>()

    val tasks = tester.absurd.claimTasks(workerId = "worker-1")
    println(tasks)
  }
}

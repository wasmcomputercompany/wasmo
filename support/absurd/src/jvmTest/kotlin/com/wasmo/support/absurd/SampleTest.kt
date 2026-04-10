package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.matches
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.Channel
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
  private val tester = AbsurdTester()

  private val uuidRegex = "[a-z0-9-]{36}"
  private val workerId = "localhost:1234"

  @Test
  fun sample() = runTest {
    val log = Channel<String>(capacity = Int.MAX_VALUE)

    val provisionUser = TaskName<ProvisionUserParams, ProvisionUserResult>("provision-user")
    tester.absurd.registerTask(
      name = provisionUser,
      taskHandler = object : TaskHandler<ProvisionUserParams, ProvisionUserResult> {
        context(context: TaskHandler.Context<ProvisionUserParams, ProvisionUserResult>)
        override suspend fun handle(params: ProvisionUserParams): ProvisionUserResult {
          val user = context.step("create-user-record") {
            log.send("${context.taskId} creating user record for ${params.userId}")
            UserRecord(
              userId = params.userId,
              email = params.email,
              createdAt = tester.clock.now(),
            )
          }

          // Demo only: fail once after the first checkpoint so the retry
          // behavior is visible.
          val outage = context.beginStep<OutageState>("demo-transient-outage")
          if (!outage.done) {
            log.send("${context.taskId} simulating a temporary email provider outage")
            outage.complete(OutageState(simulated = true))
            throw Exception("temporary email provider outage")
          }

          val delivery = context.step("send-activation-email") {
            log.send("${context.taskId} sending activation email to ${user.email}")
            DeliveryResult(
              sent = true,
              provider = "demo-mail",
              to = user.email,
            )
          }

          log.send("${context.taskId} waiting for user-activated:${user.userId}")

          // TODO: actually await event
          val activation = context.awaitEvent<ActivationEvent?>(
            event = "user-activated:${user.userId}",
            timeout = 3600.seconds,
          ) ?: ActivationEvent(tester.clock.now())

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

    val taskResultBefore = tester.absurd.fetchTaskResult(
      taskId = spawnResult.taskId,
      taskName = provisionUser,
    )

    assertThat(taskResultBefore)
      .isNotNull()
      .isInstanceOf<TaskResult.Pending<*, *>>()

    assertThat(log.tryReceive().getOrNull()).isNull()

    val batch1TaskCount = tester.absurd.executeBatch(
      workerId = workerId,
    )
    assertThat(batch1TaskCount).isEqualTo(1)
    assertThat(log.receive())
      .matches(Regex("$uuidRegex creating user record for alice"))
    assertThat(log.receive())
      .matches(Regex("$uuidRegex simulating a temporary email provider outage"))
    assertThat(log.tryReceive().getOrNull()).isNull()

    val batch2TaskCount = tester.absurd.executeBatch(
      workerId = workerId,
    )
    assertThat(batch2TaskCount).isEqualTo(1)
    assertThat(log.receive())
      .matches(Regex("$uuidRegex sending activation email to alice@example.com"))
    assertThat(log.receive())
      .matches(Regex("$uuidRegex waiting for user-activated:alice"))
    assertThat(log.tryReceive().getOrNull()).isNull()
  }
}

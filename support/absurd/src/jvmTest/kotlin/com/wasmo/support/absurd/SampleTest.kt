package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
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

/**
 * This test walks through the Absurd sample from the quick start guide. It's a broad tour of the
 * features of the system.
 */
@OptIn(ExperimentalUuidApi::class)
class SampleTest {
  @InterceptTest
  private val tester = AbsurdTester()

  private val workerId = "localhost:1234"

  @Test
  fun sample() = runTest {
    val log = Channel<String>(capacity = Int.MAX_VALUE)

    val taskHandler = object : TaskHandler<ProvisionUserParams, ProvisionUserResult> {
      context(context: TaskHandler.Context)
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

        val activation = context.awaitEvent<ActivationEvent>(
          eventName = "user-activated:${user.userId}",
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
    }

    val taskName = TaskName<ProvisionUserParams, ProvisionUserResult>("provision-user")
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = taskName,
        taskHandler = taskHandler,
      ),
    )

    val spawnResult = absurd.spawn(
      taskName,
      ProvisionUserParams(
        userId = "alice",
        email = "alice@example.com",
      ),
    )

    // Nothing executes until we call executeBatch().
    assertThat(log.receiveAvailable()).isEmpty()
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isNotNull()
      .isInstanceOf<TaskResult.Pending<*, *>>()

    // Attempt 1 fails due to a synthetic outage.
    assertThat(absurd.executeBatch(workerId))
      .isEqualTo(1)
    assertThat(log.receiveAvailable()).containsExactly(
      "${spawnResult.taskId} creating user record for alice",
      "${spawnResult.taskId} simulating a temporary email provider outage",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isNotNull()
      .isInstanceOf<TaskResult.Pending<*, *>>()

    // Attempt 2 suspends waiting for an event.
    assertThat(absurd.executeBatch(workerId))
      .isEqualTo(1)
    assertThat(log.receiveAvailable()).containsExactly(
      "${spawnResult.taskId} sending activation email to alice@example.com",
      "${spawnResult.taskId} waiting for user-activated:alice",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isNotNull()
      .isInstanceOf<TaskResult.Pending<*, *>>()

    // Attempt 3 completes.
    absurd.emitEvent(
      eventName = "user-activated:alice",
      payload = ActivationEvent(
        activatedAt = Instant.parse("2026-04-02T12:00:00Z"),
      ),
    )
    assertThat(absurd.executeBatch(workerId)).isEqualTo(1)
    assertThat(log.receiveAvailable()).containsExactly(
      "${spawnResult.taskId} waiting for user-activated:alice",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isEqualTo(
        TaskResult.Completed(
          result = ProvisionUserResult(
            userId = "alice",
            email = "alice@example.com",
            delivery = DeliveryResult(
              sent = true,
              provider = "demo-mail",
              to = "alice@example.com",
            ),
            status = "active",
            activatedAt = Instant.parse("2026-04-02T12:00:00Z"),
          ),
        ),
      )
  }
}

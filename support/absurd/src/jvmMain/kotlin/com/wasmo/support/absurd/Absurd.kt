@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import okio.utf8Size

fun Absurd(
  clock: Clock,
  postgresql: Postgresql,
  registrations: List<TaskRegistration<*, *>>,
  queueName: QueueName = QueueName.Default,
): Absurd = RealAbsurd(
  clock = clock,
  postgresql = postgresql,
  queueName = queueName,
  registrations = registrations,
)

abstract class Absurd {
  abstract suspend fun createQueue()

  abstract suspend fun <P : Any> spawn(
    taskName: TaskName<P, *>,
    params: P,
    maxAttempts: Int? = null,
    retryStrategy: RetryStrategy? = null,
    headers: Headers? = null,
    cancellation: CancellationPolicy? = null,
    idempotencyKey: String? = null,
  ): SpawnResult

  abstract suspend fun <P : Any, R : Any> fetchTaskResult(
    taskId: Uuid,
    taskName: TaskName<P, R>,
  ): TaskResult<P, R>?

  /**
   * Returns the number of tasks executed. If this is lower than [batchSize], then fewer tasks
   * were eligible to be claimed.
   */
  abstract suspend fun executeBatch(
    workerId: String,
    claimTimeout: Duration = 120.seconds,
    batchSize: Int = 1,
  ): Int

  abstract suspend fun <T> emitEvent(
    eventName: String,
    serializer: KSerializer<T>,
    payload: T,
  )

  suspend inline fun <reified T> emitEvent(
    eventName: String,
    payload: T,
  ) = emitEvent(eventName, serializer<T>(), payload)
}

interface TaskHandler<P : Any, R : Any> {
  context(context: Context)
  suspend fun handle(params: P): R

  abstract class Context {
    abstract val taskId: Uuid
    abstract val taskName: TaskName<*, *>
    abstract val headers: Headers?

    abstract suspend fun <T> step(
      name: String,
      serializer: KSerializer<T>,
      block: suspend () -> T,
    ): T

    suspend inline fun <reified T> step(
      name: String,
      noinline block: suspend () -> T,
    ): T = step(name, serializer<T>(), block)

    abstract suspend fun <T> beginStep(
      name: String,
      serializer: KSerializer<T>,
    ): StepHandle<T>

    suspend inline fun <reified T> beginStep(
      name: String,
    ): StepHandle<T> = beginStep(name, serializer<T>())

    abstract suspend fun <T> awaitEvent(
      eventName: String,
      serializer: KSerializer<T>,
      stepName: String = "\$awaitEvent:$eventName",
      timeout: Duration? = null,
    ): T

    suspend inline fun <reified T> awaitEvent(
      eventName: String,
      stepName: String = "\$awaitEvent:$eventName",
      timeout: Duration? = null,
    ): T = awaitEvent(eventName, serializer<T>(), stepName, timeout)

    abstract suspend fun sleepFor(
      stepName: String,
      duration: Duration,
    )

    abstract suspend fun sleepUntil(
      stepName: String,
      wakeAt: Instant,
    )
  }
}

interface StepHandle<T> {
  val name: String
  val checkpointName: String
  val done: Boolean
  val result: T
  suspend fun complete(result: T): T
}

data class SpawnResult(
  val taskId: Uuid,
  val runId: Uuid,
  val attempt: Int,
)

@Serializable
data class Headers(
  val unused: String? = null,
)

@Serializable(with = RetryStrategySerializer::class)
data class RetryStrategy(
  val base: Duration? = null,
  val factor: Float = 1f,
  val max: Duration? = null,
)

@Serializable(with = CancellationPolicySerializer::class)
data class CancellationPolicy(
  val maxDuration: Duration? = null,
  val maxDelay: Duration? = null,
)

data class QueueName(
  val value: String,
) {
  init {
    require(value.trim() == value) { "queue name is not trimmed" }
    require(value.isNotEmpty()) { "queue name is empty" }
    require(value.utf8Size() <= MAX_LENGTH) {
      "queue name $value is too long (max $MAX_LENGTH bytes)."
    }
  }

  override fun toString() = value

  companion object {
    private const val MAX_LENGTH = 57
    val Default = QueueName("default")
  }
}

data class TaskName<P : Any, R : Any>(
  val value: String,
  val paramsSerializer: KSerializer<P>,
  val resultSerializer: KSerializer<R>,
) {
  override fun toString() = value

  companion object {
    inline operator fun <reified P : Any, reified R : Any> invoke(value: String): TaskName<P, R> =
      TaskName(value, serializer<P>(), serializer<R>())
  }
}

data class TaskRegistration<P : Any, R : Any>(
  val taskName: TaskName<P, R>,
  val taskHandler: TaskHandler<P, R>,
  val defaultMaxAttempts: Int? = 5,
  val defaultCancellation: CancellationPolicy? = null,
)

/** Error thrown when waiting for an event or task result times out. */
class TimeoutTaskException(message: String) : CancellationException(message)

sealed interface TaskResult<P, R> {
  data class Pending<P, R>(val unused: Unit = Unit) : TaskResult<P, R>
  data class Running<P, R>(val unused: Unit = Unit) : TaskResult<P, R>
  data class Sleeping<P, R>(val unused: Unit = Unit) : TaskResult<P, R>
  data class Completed<P, R>(val result: R) : TaskResult<P, R>
  data class Failed<P, R>(
    val message: String,
    val throwableClassName: String? = null,
    val stacktrace: String? = null,
  ) : TaskResult<P, R>
  data class Cancelled<P, R>(val unused: Unit = Unit) : TaskResult<P, R>
}

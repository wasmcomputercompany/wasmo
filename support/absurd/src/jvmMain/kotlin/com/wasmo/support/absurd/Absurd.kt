@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
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
  queueName: QueueName = QueueName.Default,
  defaultMaxAttempts: Int = 5,
): Absurd = RealAbsurd(
  clock = clock,
  postgresql = postgresql,
  queueName = queueName,
  defaultMaxAttempts = defaultMaxAttempts,
)

interface Absurd {
  suspend fun createQueue(
    queueName: QueueName? = null,
  )

  suspend fun <P : Any, R : Any> registerTask(
    name: TaskName<P, R>,
    queueName: QueueName? = null,
    defaultMaxAttempts: Int? = null,
    defaultCancellation: CancellationPolicy? = null,
    taskHandler: TaskHandler<P, R>,
  )

  suspend fun <P : Any> spawn(
    taskName: TaskName<P, *>,
    params: P,
    maxAttempts: Int? = null,
    retryStrategy: RetryStrategy? = null,
    headers: Headers? = null,
    queueName: QueueName? = null,
    cancellation: CancellationPolicy? = null,
    idempotencyKey: String? = null,
  ): SpawnResult

  suspend fun <P : Any, R : Any> fetchTaskResult(
    taskId: Uuid,
    taskName: TaskName<P, R>,
    queueName: QueueName? = null,
  ): TaskResult<P, R>?

  /**
   * Returns the number of tasks executed. If this is lower than [batchSize], then fewer tasks
   * were eligible to be claimed.
   */
  suspend fun executeBatch(
    workerId: String,
    claimTimeout: Duration = 120.seconds,
    batchSize: Int = 1,
  ): Int
}

interface TaskHandler<P : Any, R : Any> {
  context(context: Context<P, R>)
  suspend fun handle(params: P): R

  abstract class Context<P : Any, R : Any> {
    abstract val queueName: QueueName
    abstract val taskId: Uuid
    abstract val taskName: TaskName<P, R>
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
      event: String,
      serializer: KSerializer<T>,
      timeout: Duration,
    ): T

    suspend inline fun <reified T> awaitEvent(
      event: String,
      timeout: Duration,
    ): T = awaitEvent(event, serializer<T>(), timeout)

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

data class ClaimedTask<P : Any, R : Any>(
  val runId: Uuid,
  val taskId: Uuid,
  val attempt: Int,
  val taskName: TaskName<P, R>,
  val params: P,
  val retryStrategy: RetryStrategy?,
  val maxAttempts: Int?,
  val headers: Headers?,
  val wakeEvent: String?,
  val eventPayload: Any?,
)

data class SpawnResult(
  val taskId: Uuid,
  val runId: String,
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

sealed interface TaskResult<P, R> {
  data class Pending<P, R>(val unused: Unit = Unit) : TaskResult<P, R>
  data class Completed<P, R>(val result: R) : TaskResult<P, R>
  data class Failed<P, R>(
    val message: String,
    val throwableClassName: String? = null,
    val stacktrace: String? = null,
  ) : TaskResult<P, R>
}

package com.wasmo.support.absurd

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import okio.utf8Size

interface Absurd {
  suspend fun <P, R> registerTask(
    name: TaskName<P, R>,
    queueName: QueueName? = null,
    defaultMaxAttempts: Int? = null,
    defaultCancellation: CancellationPolicy? = null,
    taskHandler: TaskHandler<P, R>,
  )

  suspend fun <P> spawn(
    taskName: TaskName<P, *>,
    params: P,
    maxAttempts: Int? = null,
    retryStrategy: RetryStrategy? = null,
    headers: Headers? = null,
    queueName: QueueName? = null,
    cancellation: CancellationPolicy? = null,
    idempotencyKey: String? = null,
  ): SpawnResult

  suspend fun <P, R> fetchTaskResult(
    taskId: String,
    taskName: TaskName<P, R>,
    queueName: QueueName? = null,
  ): TaskResult<P, R>

  suspend fun claimTasks(
    batchSize: Int = 1,
    claimTimeout: Duration = 2.minutes,
    workerId: String,
  ): List<ClaimedTask<*, *>>

  suspend fun <P, R> completeTaskRun(
    claimedTask: ClaimedTask<P, R>,
    result: R,
  )

  suspend fun <P, R> failTaskRun(
    claimedTask: ClaimedTask<P, R>,
    error: String,
    fatalError: String? = null,
  )
}

interface TaskHandler<P, R> {
  context(context: Context<P, R>)
  fun handle(params: P): R

  interface Context<P, R> {
    val queueName: QueueName
    val taskName: TaskName<P, R>

    suspend fun <T> step(
      name: String,
      serializer: KSerializer<T>,
      block: suspend () -> T,
    ): T

    suspend fun <T> beginStep(
      name: String,
      serializer: KSerializer<T>,
    ): StepHandle<T>
  }
}

interface StepHandle<T> {
  val name: String
  val checkpointName: String
  val result: Result<T>
  suspend fun complete(result: T)
}

class ClaimedTask<P, R>(
  val runId: String,
  val taskId: String,
  val attempt: Int,
  val taskName: TaskName<P, R>,
  val params: P,
  val retryStrategy: RetryStrategy?,
  val maxAttempts: Int?,
  val headers: Headers,
  val wakeEvent: Any?,
  val eventPayload: Any?,
)

data class SpawnResult(
  val taskId: String,
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

  private companion object {
    const val MAX_LENGTH = 57
  }
}

data class TaskName<P, R>(
  val value: String,
  val paramsSerializer: KSerializer<P>,
  val outputSerializer: KSerializer<R>,
) {
  override fun toString() = value

  companion object {
    inline operator fun <reified P, reified R> invoke(value: String): TaskName<P, R> =
      TaskName(value, serializer<P>(), serializer<R>())
  }
}

sealed interface TaskResult<P, R> {
  class Pending<P, R> : TaskResult<P, R>
  data class Completed<P, R>(val result: R) : TaskResult<P, R>
  data class Failed<P, R>(val failureReason: String?) : TaskResult<P, R>
}

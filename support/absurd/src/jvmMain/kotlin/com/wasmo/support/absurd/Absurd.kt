package com.wasmo.support.absurd

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.KSerializer

interface Absurd {
  suspend fun <P, R> registerTask(
    name: TaskName<P, R>,
    queueName: QueueName = QueueName,
    defaultMaxAttempts: Int? = null,
    defaultCancellation: Int? = null,
    taskHandler: TaskHandler<P, R>,
  )

  suspend fun <P> spawn(
    taskName: TaskName<P, *>,
    params: P,
    maxAttempts: Int? = null,
    retryStrategy: RetryStrategy? = null,
    headers: Headers = Headers,
    queueName: QueueName = QueueName,
    cancellation: CancellationPolicy? = null,
    idempotencyKey: String? = null,
  ): SpawnResult

  suspend fun <P, R> fetchTaskResult(
    taskId: String,
    taskName: TaskName<P, R>,
    queueName: QueueName,
  )

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

object Headers

data class RetryStrategy(
  val base: Duration,
  val factor: Float,
  val max: Duration,
)

data class CancellationPolicy(
  val maxDuration: Duration,
  val maxDelay: Duration,
)

object QueueName

data class TaskName<P, R>(
  val value: String,
  val inputSerializer: KSerializer<P>,
  val outputSerializer: KSerializer<R>,
)

sealed interface TaskResult {
  data object Pending : TaskResult
  data class Completed<R>(val result: R) : TaskResult
  data class Failed(val failureReason: String?) : TaskResult
}

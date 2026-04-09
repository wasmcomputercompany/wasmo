package com.wasmo.support.absurd

import kotlin.time.Duration
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RealAbsurd(
  private val postgresql: Postgresql,
  private val queueName: QueueName = QueueName("default"),
  private val defaultMaxAttempts: Int = 5,
) : Absurd {
  private val registry = mutableMapOf<TaskName<*, *>, TaskRegistration<*, *>>()
  private val workerRunning = false

  override suspend fun <P, R> registerTask(
    name: TaskName<P, R>,
    queueName: QueueName?,
    defaultMaxAttempts: Int?,
    defaultCancellation: CancellationPolicy?,
    taskHandler: TaskHandler<P, R>,
  ) {
    registry[name] = TaskRegistration(
      name = name,
      queueName = queueName ?: this.queueName,
      defaultMaxAttempts = defaultMaxAttempts ?: defaultMaxAttempts,
      defaultCancellation = defaultCancellation ?: defaultCancellation,
      taskHandler = taskHandler,
    )
  }

  private class TaskRegistration<P, R>(
    val name: TaskName<P, R>,
    val queueName: QueueName,
    val defaultMaxAttempts: Int?,
    val defaultCancellation: CancellationPolicy?,
    val taskHandler: TaskHandler<P, R>,
  )

  override suspend fun <P> spawn(
    taskName: TaskName<P, *>,
    params: P,
    maxAttempts: Int?,
    retryStrategy: RetryStrategy?,
    headers: Headers?,
    queueName: QueueName?,
    cancellation: CancellationPolicy?,
    idempotencyKey: String?,
  ): SpawnResult {
    val registration = registry[taskName]
    val actualQueue: QueueName

    if (registration != null) {
      actualQueue = registration.queueName
      require(queueName == null || queueName == registration.queueName) {
        """Task "$taskName" is registered for queue "$actualQueue" but spawn """ +
          """requested queue "$queueName""""
      }
    } else {
      actualQueue = queueName
        ?: error(
          """Task "$taskName" is not registered. """ +
            """Provide queue when spawning unregistered tasks.""",
        )
    }

    val effectiveMaxAttempts = maxAttempts
      ?: registration?.defaultMaxAttempts
      ?: defaultMaxAttempts

    val effectiveCancellation = cancellation
      ?: registration?.defaultCancellation

    val options = SpawnOptions(
      max_attempts = effectiveMaxAttempts,
      retry_strategy = retryStrategy,
      headers = headers,
      cancellation = effectiveCancellation,
      idempotency_key = idempotencyKey,
    )

    postgresql.withConnection {
      val row = execute(
        """
        SELECT task_id, run_id, attempt
        FROM absurd.spawn_task(%s, %s, %s, %s)
        """,
        actualQueue,
        taskName,
        AbsurdJson.encodeToString(taskName.paramsSerializer, params),
        AbsurdJson.encodeToString(options),
      )

      val rowsUpdated = row.rowsUpdated.awaitSingle()
      println(rowsUpdated)

      // TODO: get a single row with task_id, run_id, attempt

      return SpawnResult(
        taskId = "TODO",
        runId = "TODO",
        attempt = 1,
      )
    }
  }

  override suspend fun <P, R> fetchTaskResult(
    taskId: String,
    taskName: TaskName<P, R>,
    queueName: QueueName?,
  ): TaskResult<P, R> {
    TODO("Not yet implemented")
  }

  override suspend fun claimTasks(
    batchSize: Int,
    claimTimeout: Duration,
    workerId: String,
  ): List<ClaimedTask<*, *>> {
    TODO("Not yet implemented")
  }

  override suspend fun <P, R> completeTaskRun(
    claimedTask: ClaimedTask<P, R>,
    result: R,
  ) {
  }

  override suspend fun <P, R> failTaskRun(
    claimedTask: ClaimedTask<P, R>,
    error: String,
    fatalError: String?,
  ) {
    TODO("Not yet implemented")
  }
}

@Serializable
internal data class SpawnOptions(
  val headers: Headers? = null,
  val max_attempts: Int? = null,
  val retry_strategy: RetryStrategy? = null,
  val cancellation: CancellationPolicy? = null,
  val idempotency_key: String? = null,
)

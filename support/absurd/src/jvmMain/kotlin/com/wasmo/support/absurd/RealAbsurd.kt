@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.Readable
import java.util.UUID
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable

class RealAbsurd(
  private val postgresql: Postgresql,
  private val queueName: QueueName = QueueName.Default,
  private val defaultMaxAttempts: Int = 5,
) : Absurd {
  private val registry = mutableMapOf<TaskName<*, *>, TaskRegistration<*, *>>()
  private val workerRunning = false

  override suspend fun createQueue(queueName: QueueName?) {
    postgresql.withConnection {
      val statement = createStatement(
        """
        SELECT absurd.create_queue($1)
        """,
        (queueName ?: this@RealAbsurd.queueName).value,
      )
      statement.execute().awaitSingle()
    }
  }

  override suspend fun <P : Any, R : Any> registerTask(
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

  private class TaskRegistration<P : Any, R : Any>(
    val name: TaskName<P, R>,
    val queueName: QueueName,
    val defaultMaxAttempts: Int?,
    val defaultCancellation: CancellationPolicy?,
    val taskHandler: TaskHandler<P, R>,
  )

  override suspend fun <P : Any> spawn(
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
      val statement = createStatement(
        """
        SELECT task_id, run_id, attempt
        FROM absurd.spawn_task($1, $2, $3, $4)
        """,
        actualQueue.value,
        taskName.value,
        Json.of(KotlinJson.encodeToString(taskName.paramsSerializer, params)),
        Json.of(KotlinJson.encodeToString(options)),
      )
      val result = statement.execute().awaitSingle()
      val map = result.map {
        SpawnResult(
          taskId = it.get("task_id", UUID::class.java)!!.toKotlinUuid(),
          runId = it.get("run_id", String::class.java)!!,
          attempt = it.get("attempt", Int::class.java)!!,
        )
      }
      return map.awaitSingle()
    }
  }

  override suspend fun <P : Any, R : Any> fetchTaskResult(
    taskId: Uuid,
    taskName: TaskName<P, R>,
    queueName: QueueName?,
  ): TaskResult<P, R>? {
    postgresql.withConnection {
      val statement = createStatement(
        """
        SELECT state, result, failure_reason
        FROM absurd.get_task_result($1, $2)
        """,
        (queueName ?: this@RealAbsurd.queueName).value,
        taskId.toJavaUuid(),
      )
      val result = statement.execute().awaitSingle()
      val map = result.map {
        val state = it.get("state")
        when (state) {
          "completed" -> TaskResult.Completed<P, R>(it.get("result") as R)
          "failed" -> TaskResult.Failed<P, R>(it.get("failure_reason") as String)
          else -> TaskResult.Pending<P, R>()
        }
      }
      return map.awaitFirstOrNull()
    }
  }

  override suspend fun claimTasks(
    batchSize: Int,
    claimTimeout: Duration,
    workerId: String,
  ): List<ClaimedTask<*, *>> {
    postgresql.withConnection {
      val statement = createStatement(
        """
        SELECT run_id, task_id, attempt, task_name, params, retry_strategy, max_attempts,
               headers, wake_event, event_payload
        FROM absurd.claim_task($1, $2, $3, $4)
        """,
        queueName.value,
        workerId,
        claimTimeout.inWholeSeconds.toInt(),
        batchSize,
      )
      val result = statement.execute().awaitSingle()
      val map = result.map {
        val taskNameValue = it.get("task_name")
        val taskName = registry.keys.singleOrNull { it.value == taskNameValue }
          ?: error("task is not registered: $taskNameValue")
        it.getClaimedTask(taskName)
      }
      return map.asFlow().toList()
    }
  }

  private fun <P : Any, R : Any> Readable.getClaimedTask(taskName: TaskName<P, R>) = ClaimedTask(
    runId = uuid("run_id"),
    taskId = uuid("task_id"),
    attempt = int("attempt"),
    taskName = taskName,
    params = json("params", taskName.paramsSerializer),
    retryStrategy = jsonOrNull<RetryStrategy>("retry_strategy"),
    maxAttempts = int("max_attempts"),
    headers = jsonOrNull<Headers>("headers"),
    wakeEvent = stringOrNull("wake_event"),
    eventPayload = get("event_payload", Json::class.java),
  )

  override suspend fun <P : Any, R : Any> completeTaskRun(
    claimedTask: ClaimedTask<P, R>,
    result: R,
  ) {
  }

  override suspend fun <P : Any, R : Any> failTaskRun(
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

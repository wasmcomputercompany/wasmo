@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory as Postgresql
import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.Readable
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

internal class RealAbsurd(
  private val clock: Clock,
  private val postgresql: Postgresql,
  private val queueName: QueueName = QueueName.Default,
  private val defaultMaxAttempts: Int = 5,
) : Absurd {
  private val registry = mutableMapOf<TaskName<*, *>, TaskRegistration<*, *>>()

  override suspend fun createQueue(queueName: QueueName?) {
    postgresql.withConnection {
      execute(
        """SELECT absurd.create_queue($1)""",
        (queueName ?: this@RealAbsurd.queueName).value,
      )
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
      defaultMaxAttempts = defaultMaxAttempts ?: this.defaultMaxAttempts,
      defaultCancellation = defaultCancellation,
      taskHandler = taskHandler,
    )
  }

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
        """Task "$taskName" is registered for queue "$actualQueue" """ +
          """but spawn requested queue "$queueName""""
      }
    } else {
      actualQueue = queueName
        ?: error("""Task "$taskName" is not registered.""")
    }

    val effectiveMaxAttempts = maxAttempts
      ?: registration?.defaultMaxAttempts
      ?: defaultMaxAttempts

    val effectiveCancellation = cancellation
      ?: registration?.defaultCancellation

    val options = SpawnOptionsJson(
      max_attempts = effectiveMaxAttempts,
      retry_strategy = retryStrategy,
      headers = headers,
      cancellation = effectiveCancellation,
      idempotency_key = idempotencyKey,
    )

    postgresql.withConnection {
      val rows = executeQuery(
        """
        SELECT task_id, run_id, attempt
        FROM absurd.spawn_task($1, $2, $3, $4)
        """,
        actualQueue.value,
        taskName.value,
        Json.of(KotlinJson.encodeToString(taskName.paramsSerializer, params)),
        Json.of(KotlinJson.encodeToString(options)),
      ) {
        SpawnResult(
          taskId = get("task_id", UUID::class.java)!!.toKotlinUuid(),
          runId = get("run_id", UUID::class.java)!!.toKotlinUuid(),
          attempt = get("attempt", Int::class.java)!!,
        )
      }
      return rows.single()
    }
  }

  override suspend fun <P : Any, R : Any> fetchTaskResult(
    taskId: Uuid,
    taskName: TaskName<P, R>,
    queueName: QueueName?,
  ): TaskResult<P, R>? {
    postgresql.withConnection {
      val rows: List<TaskResult<P, R>> = executeQuery(
        """
        SELECT state, result, failure_reason
        FROM absurd.get_task_result($1, $2)
        """,
        (queueName ?: this@RealAbsurd.queueName).value,
        taskId.toJavaUuid(),
      ) {
        val state = get("state")
        when (state) {
          "completed" -> TaskResult.Completed(
            result = json("result", taskName.resultSerializer),
          )

          "failed" -> {
            val failureReason = json<TaskErrorJson>("failure_reason")
            TaskResult.Failed(
              message = failureReason.message,
              throwableClassName = failureReason.name,
              stacktrace = failureReason.traceback,
            )
          }

          else -> TaskResult.Pending()
        }
      }
      return rows.singleOrNull()
    }
  }

  override suspend fun executeBatch(
    workerId: String,
    claimTimeout: Duration,
    batchSize: Int,
  ): Int {
    val tasks = claimTasks(
      batchSize = batchSize,
      claimTimeout = claimTimeout,
      workerId = workerId,
    )

    for (task in tasks) {
      executeTask(task, claimTimeout)
    }

    return tasks.size
  }

  private suspend fun <P : Any, R : Any> executeTask(
    task: ClaimedTask<P, R>,
    claimTimeout: Duration,
  ) {
    @Suppress("UNCHECKED_CAST") // registry keys and values always have identical types.
    val registration = registry[task.taskName] as TaskRegistration<P, R>?

    if (registration == null) {
      failTaskRun(
        claimedTask = task,
        error = TaskErrorJson("unknown task"),
      )
      return
    }

    val context = createTaskContext(
      queueName = queueName,
      task = task,
      claimTimeout = claimTimeout,
    )

    try {
      val result = context(context) {
        registration.taskHandler.handle(task.params)
      }
      completeTaskRun(
        queueName = queueName,
        claimedTask = task,
        result = result,
      )
    } catch (_: CanceledTaskException) {
    } catch (_: SuspendTaskException) {
    } catch (_: FailedTaskException) {
    } catch (e: Throwable) {
      failTaskRun(
        claimedTask = task,
        error = TaskErrorJson(e),
      )
    }
  }

  private suspend fun <P : Any, R : Any> createTaskContext(
    queueName: QueueName,
    task: ClaimedTask<P, R>,
    claimTimeout: Duration,
  ): TaskHandler.Context {
    return postgresql.withConnection {
      val rows = executeQuery(
        """
        SELECT checkpoint_name, state, status, owner_run_id, updated_at
        FROM absurd.get_task_checkpoint_states($1, $2, $3)
        """,
        queueName.value,
        task.taskId.toJavaUuid(),
        task.runId.toJavaUuid(),
      ) {
        string("checkpoint_name") to rawJson("state")
      }
      RealTaskContext(
        queueName = queueName,
        task = task,
        checkpointCache = rows.toMap().toMutableMap(),
        claimTimeout = claimTimeout,
      )
    }
  }

  private suspend fun claimTasks(
    batchSize: Int,
    claimTimeout: Duration,
    workerId: String,
  ): List<ClaimedTask<*, *>> {
    postgresql.withConnection {
      return executeQuery(
        """
        SELECT run_id, task_id, attempt, task_name, params, retry_strategy, max_attempts,
               headers, wake_event, event_payload
        FROM absurd.claim_task($1, $2, $3, $4)
        """,
        queueName.value,
        workerId,
        claimTimeout.inWholeSeconds.toInt(),
        batchSize,
      ) {
        val taskNameValue = get("task_name")
        val taskName = registry.keys.singleOrNull { it.value == taskNameValue }
          ?: error("task is not registered: $taskNameValue")
        getClaimedTask(taskName)
      }
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

  private suspend fun <P : Any, R : Any> completeTaskRun(
    queueName: QueueName,
    claimedTask: ClaimedTask<P, R>,
    result: R,
  ) {
    postgresql.withConnection {
      execute(
        "SELECT absurd.complete_run($1, $2, $3)",
        queueName.value,
        claimedTask.runId.toJavaUuid(),
        Json.of(KotlinJson.encodeToString(claimedTask.taskName.resultSerializer, result)),
      )
    }
  }

  private suspend fun <P : Any, R : Any> failTaskRun(
    claimedTask: ClaimedTask<P, R>,
    error: TaskErrorJson,
    retryAt: Instant? = null,
  ) {
    postgresql.withConnection {
      execute(
        "SELECT absurd.fail_run($1, $2, $3, $4)",
        queueName.value,
        claimedTask.runId.toJavaUuid(),
        Json.of(KotlinJson.encodeToString(error)),
        retryAt?.toJavaInstant(),
      )
    }
  }

  private inner class RealTaskContext<P : Any, R : Any>(
    override val queueName: QueueName,
    private val task: ClaimedTask<P, R>,
    private val checkpointCache: MutableMap<String, Json>,
    private val claimTimeout: Duration,
  ) : TaskHandler.Context() {
    private val stepNameCounter = mutableMapOf<String, Int>()

    override val taskId: Uuid
      get() = task.taskId
    override val taskName: TaskName<P, R>
      get() = task.taskName
    override val headers: Headers?
      get() = task.headers

    override suspend fun <T> step(
      name: String,
      serializer: KSerializer<T>,
      block: suspend () -> T,
    ): T {
      val handle = beginStep(name, serializer)
      if (handle.done) {
        return handle.result
      }

      val result = block()
      return handle.complete(result)
    }

    override suspend fun <T> beginStep(
      name: String,
      serializer: KSerializer<T>,
    ): StepHandle<T> {
      val checkpointName = takeCheckpointName(name)
      val state = lookupCheckpoint(checkpointName)
      if (state !== CHECKPOINT_NOT_FOUND) {
        return RealStepHandle(
          name = name,
          checkpointName = checkpointName,
          serializer = serializer,
          done = true,
          resultOrNull = KotlinJson.decodeFromString(serializer, state.asString()),
        )
      }

      return RealStepHandle(
        name = name,
        checkpointName = checkpointName,
        serializer = serializer,
      )
    }

    private fun takeCheckpointName(name: String): String {
      val count = stepNameCounter.getOrDefault(name, 0) + 1
      stepNameCounter[name] = count
      return when {
        count == 1 -> name
        else -> "$name#$count"
      }
    }

    suspend fun <T> persistCheckpoint(
      checkpointName: String,
      serializer: KSerializer<T>,
      value: T,
    ) {
      val valueJson = Json.of(KotlinJson.encodeToString(serializer, value))
      postgresql.withConnection {
        try {
          execute(
            "SELECT absurd.set_task_checkpoint_state($1, $2, $3, $4, $5, $6)",
            queueName.value,
            task.taskId.toJavaUuid(),
            checkpointName,
            valueJson,
            task.runId.toJavaUuid(),
            claimTimeout.inWholeSeconds.toInt(),
          )
        } catch (e: Exception) {
          throw e // TODO
        }
        checkpointCache[checkpointName] = valueJson
      }
    }

    private suspend fun lookupCheckpoint(checkpointName: String): Json {
      val cached = this.checkpointCache[checkpointName]
      if (cached != null) return cached

      postgresql.withConnection {
        val rows = executeQuery(
          """
          SELECT checkpoint_name, state, status, owner_run_id, updated_at
          FROM absurd.get_task_checkpoint_state($1, $2, $3)
          """,
          queueName.value,
          task.taskId.toJavaUuid(),
          checkpointName,
        ) {
          rawJson("state")
        }
        if (rows.isNotEmpty()) {
          val state = rows.single()
          checkpointCache[checkpointName] = state
          return state
        }

        return CHECKPOINT_NOT_FOUND
      }
    }

    override suspend fun <T> awaitEvent(
      event: String,
      serializer: KSerializer<T>,
      timeout: Duration,
    ): T {
      return null as T // TODO
    }

    override suspend fun sleepFor(stepName: String, duration: Duration) {
      val now = clock.now()
      sleepUntil(stepName, now, now + duration)
    }

    override suspend fun sleepUntil(stepName: String, wakeAt: Instant) {
      sleepUntil(stepName, clock.now(), wakeAt)
    }

    private suspend fun sleepUntil(
      stepName: String,
      now: Instant,
      wakeAt: Instant,
    ) {
      val checkpointName = takeCheckpointName(stepName)
      val state = lookupCheckpoint(checkpointName)

      val actualWakeAt = when {
        state === CHECKPOINT_NOT_FOUND -> {
          persistCheckpoint(checkpointName, Instant.serializer(), wakeAt)
          wakeAt
        }

        else -> KotlinJson.decodeFromString<Instant>(state.asString())
      }

      if (now < actualWakeAt) {
        scheduleRun(actualWakeAt)
        throw SuspendTaskException()
      }
    }

    private suspend fun scheduleRun(wakeAt: Instant) {
      postgresql.withConnection {
        execute(
          "SELECT absurd.schedule_run($1, $2, $3)",
          queueName.value,
          task.runId.toJavaUuid(),
          wakeAt.toJavaInstant(),
        )
      }
    }

    inner class RealStepHandle<T>(
      override val name: String,
      override val checkpointName: String,
      private val serializer: KSerializer<T>,
      override var done: Boolean = false,
      private var resultOrNull: T? = null,
    ) : StepHandle<T> {
      override val result: T
        get() {
          check(done)
          return resultOrNull!!
        }

      override suspend fun complete(result: T): T {
        if (done) return result

        done = true
        resultOrNull = result
        persistCheckpoint(checkpointName, serializer, result)
        return result
      }
    }
  }

  companion object {
    /** Sentinel value when we haven't completed a checkpoint. Compare with `===`. */
    private val CHECKPOINT_NOT_FOUND = Json.of("[\"CHECKPOINT_NOT_FOUND\"]")
  }
}

private class CanceledTaskException : CancellationException()
private class SuspendTaskException : CancellationException()
private class FailedTaskException : CancellationException()

internal data class TaskRegistration<P : Any, R : Any>(
  val name: TaskName<P, R>,
  val queueName: QueueName,
  val defaultMaxAttempts: Int?,
  val defaultCancellation: CancellationPolicy?,
  val taskHandler: TaskHandler<P, R>,
)

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

@Serializable
@Suppress("PropertyName") // Consistent JSON with other Absurd SDKs.
internal data class SpawnOptionsJson(
  val headers: Headers? = null,
  val max_attempts: Int? = null,
  val retry_strategy: RetryStrategy? = null,
  val cancellation: CancellationPolicy? = null,
  val idempotency_key: String? = null,
)

@Serializable
internal data class TaskErrorJson(
  val message: String,
  val name: String? = null,
  val traceback: String? = null,
) {
  constructor(e: Throwable) : this(
    message = e.message ?: e::class.simpleName ?: e.toString(),
    name = e::class.qualifiedName,
    traceback = e.stackTraceToString(),
  )
}

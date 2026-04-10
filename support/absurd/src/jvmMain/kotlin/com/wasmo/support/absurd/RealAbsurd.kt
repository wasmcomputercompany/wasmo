@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import io.r2dbc.postgresql.PostgresqlConnectionFactory
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

class RealAbsurd(
  private val postgresql: PostgresqlConnectionFactory,
  private val queueName: QueueName = QueueName.Default,
  private val defaultMaxAttempts: Int = 5,
) : Absurd {
  private val registry = mutableMapOf<TaskName<*, *>, TaskRegistration<*, *>>()

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

    val options = SpawnOptionsJson(
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

  override suspend fun executeOneBatch(
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
    val registration = registry[task.taskName] as TaskRegistration<P, R>?

    if (registration == null) {
      failTaskRun(
        claimedTask = task,
        error = "unknown task",
      )
      return
    }

    val context = createTaskContext(
      queueName = queueName,
      task = task,
      claimTimeout = claimTimeout,
    )

    val result = context(context) {
      registration.taskHandler.handle(task.params)
    }

    completeTaskRun(
      queueName = queueName,
      claimedTask = task,
      result = result,
    )
  }

  private suspend fun <P : Any, R : Any> createTaskContext(
    queueName: QueueName,
    task: ClaimedTask<P, R>,
    claimTimeout: Duration,
  ): TaskHandler.Context<P, R> {
    return postgresql.withConnection {
      val statement = createStatement(
        """
        SELECT checkpoint_name, state, status, owner_run_id, updated_at
        FROM absurd.get_task_checkpoint_states($1, $2, $3)
        """,
        queueName.value,
        task.taskId.toJavaUuid(),
        task.runId.toJavaUuid(),
      )
      val result = statement.execute().awaitSingle()
      val map = result.map {
        it.string("checkpoint_name") to it.rawJsonOrNull("state")
      }
      val cache = map.asFlow().toList().toMap().toMutableMap()
      RealTaskContext(
        queueName = queueName,
        task = task,
        checkpointCache = cache,
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

  private suspend fun <P : Any, R : Any> completeTaskRun(
    queueName: QueueName,
    claimedTask: ClaimedTask<P, R>,
    result: R,
  ) {
    postgresql.withConnection {
      val statement = createStatement(
        "SELECT absurd.complete_run($1, $2, $3)",
        queueName.value,
        claimedTask.runId,
        Json.of(KotlinJson.encodeToString(claimedTask.taskName.outputSerializer, result)),
      )
      statement.execute().awaitSingle()
    }
  }

  private suspend fun <P : Any, R : Any> failTaskRun(
    claimedTask: ClaimedTask<P, R>,
    error: String,
    fatalError: String? = null,
  ) {
    postgresql.withConnection {
      val statement = createStatement(
        "SELECT absurd.fail_run($1, $2, $3, $4)",
        queueName.value,
        claimedTask.runId,
        error,
        fatalError,
      )
      statement.execute().awaitSingle()
    }
  }

  private inner class RealTaskContext<P : Any, R : Any>(
    override val queueName: QueueName,
    private val task: ClaimedTask<P, R>,
    private val checkpointCache: MutableMap<String, Json>,
    private val claimTimeout: Duration,
  ) : TaskHandler.Context<P, R>() {
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
          val statement = createStatement(
            "SELECT absurd.set_task_checkpoint_state($1, $2, $3, $4, $5, $6)",
            queueName.value,
            task.taskId.toJavaUuid(),
            checkpointName,
            valueJson,
            task.runId.toJavaUuid(),
            claimTimeout.inWholeSeconds.toInt(),
          )
          statement.execute().awaitSingle()
        } catch (e: Exception) {
          throw e
        }
        checkpointCache[checkpointName] = valueJson
      }
    }

    private suspend fun lookupCheckpoint(checkpointName: String): Json {
      val cached = this.checkpointCache[checkpointName]
      if (cached != null) return cached

      postgresql.withConnection {
        val statement = createStatement(
          """
          SELECT checkpoint_name, state, status, owner_run_id, updated_at
          FROM absurd.get_task_checkpoint_state($1, $2, $3)
          """,
          queueName.value,
          task.taskId.toJavaUuid(),
          checkpointName,
        )
        val rows = statement.execute().awaitSingle()
          .map { it.rawJsonOrNull("state") }
          .asFlow()
          .toList()
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

internal data class TaskRegistration<P : Any, R : Any>(
  val name: TaskName<P, R>,
  val queueName: QueueName,
  val defaultMaxAttempts: Int?,
  val defaultCancellation: CancellationPolicy?,
  val taskHandler: TaskHandler<P, R>,
)

@Serializable
internal data class SpawnOptionsJson(
  val headers: Headers? = null,
  val max_attempts: Int? = null,
  val retry_strategy: RetryStrategy? = null,
  val cancellation: CancellationPolicy? = null,
  val idempotency_key: String? = null,
)

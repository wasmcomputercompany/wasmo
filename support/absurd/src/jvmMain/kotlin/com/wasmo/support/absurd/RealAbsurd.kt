package com.wasmo.support.absurd

import kotlin.time.Duration

class RealAbsurd(
  private val postgresql: Postgresql
) : Absurd {
  override suspend fun <P, R> registerTask(
    name: TaskName<P, R>,
    queueName: QueueName,
    defaultMaxAttempts: Int?,
    defaultCancellation: Int?,
    taskHandler: TaskHandler<P, R>,
  ) {
    TODO("Not yet implemented")
  }

  override suspend fun <P> spawn(
    taskName: TaskName<P, *>,
    params: P,
    maxAttempts: Int?,
    retryStrategy: RetryStrategy?,
    headers: Headers,
    queueName: QueueName,
    cancellation: CancellationPolicy?,
    idempotencyKey: String?,
  ): SpawnResult {
    TODO("Not yet implemented")
  }

  override suspend fun <P, R> fetchTaskResult(
    taskId: String,
    taskName: TaskName<P, R>,
    queueName: QueueName,
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
    TODO("Not yet implemented")
  }

  override suspend fun <P, R> failTaskRun(
    claimedTask: ClaimedTask<P, R>,
    error: String,
    fatalError: String?,
  ) {
    TODO("Not yet implemented")
  }
}

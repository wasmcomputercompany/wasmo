@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.jobs

import com.wasmo.identifiers.JobName
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Wasmo's job interface is a thin wrapper around Absurd's API.
 */
interface OsJobHandler<P : Any, R : Any> {
  context(context: Context)
  suspend fun handle(job: P): R

  abstract class Context {
    abstract val jobId: Uuid
    abstract val jobName: JobName<*, *>

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

data class JobRegistration<P : Any, R : Any>(
  val jobName: JobName<P, R>,
  val jobHandler: OsJobHandler<P, R>,
  val defaultMaxAttempts: Int? = 5,
  val defaultCancellation: CancellationPolicy? = null,
)

data class CancellationPolicy(
  val maxDuration: Duration? = null,
  val maxDelay: Duration? = null,
)

@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.jobs.absurd

import com.wasmo.identifiers.JobName
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.CancellationPolicy
import com.wasmo.jobs.JobRegistration
import com.wasmo.jobs.OsJobHandler
import com.wasmo.jobs.StepHandle
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.support.absurd.Absurd
import com.wasmo.support.absurd.CancellationPolicy as AbsurdCancellationPolicy
import com.wasmo.support.absurd.PostgresqlClient
import com.wasmo.support.absurd.StepHandle as AbsurdStepHandle
import com.wasmo.support.absurd.TaskHandler as AbsurdJobHandler
import com.wasmo.support.absurd.TaskName as AbsurdJobName
import com.wasmo.support.absurd.TaskRegistration as AbsurdJobRegistration
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.KSerializer

@Inject
@SingleIn(OsScope::class)
class AbsurdService(
  clock: Clock,
  postgresqlAddress: PostgresqlAddress,
  registrations: List<JobRegistration<*, *>>,
) {
  val absurd: Absurd = Absurd(
    clock = clock,
    postgresql = PostgresqlClient(
      PgConnectOptions()
        .setHost(postgresqlAddress.hostname)
        .setDatabase(postgresqlAddress.databaseName)
        .setUser(postgresqlAddress.user)
        .setPassword(postgresqlAddress.password)
        .setSslMode(
          when {
            postgresqlAddress.ssl -> SslMode.VERIFY_FULL
            else -> SslMode.DISABLE
          },
        ),
    ),
    registrations = registrations.map { it.toAbsurd() },
  )

  suspend fun createQueue() {
    absurd.createQueue()
  }
}

internal fun <P : Any, R : Any> JobRegistration<P, R>.toAbsurd() = AbsurdJobRegistration(
  taskName = jobName.toAbsurd(),
  taskHandler = jobHandler.toAbsurd(),
  defaultMaxAttempts = defaultMaxAttempts,
  defaultCancellation = defaultCancellation?.toAbsurd(),
)

internal fun <P : Any, R : Any> JobName<P, R>.toAbsurd() = AbsurdJobName(
  value = value,
  paramsSerializer = paramsSerializer,
  resultSerializer = resultSerializer,
)

internal fun <P : Any, R : Any> OsJobHandler<P, R>.toAbsurd() = RealTaskHandler(this)

internal fun <P : Any, R : Any> AbsurdJobName<P, R>.toWasmo() = JobName(
  value = value,
  paramsSerializer = paramsSerializer,
  resultSerializer = resultSerializer,
)

internal fun CancellationPolicy.toAbsurd() = AbsurdCancellationPolicy(
  maxDuration = maxDuration,
  maxDelay = maxDelay,
)

internal class RealTaskHandler<P : Any, R : Any>(
  private val delegate: OsJobHandler<P, R>,
) : AbsurdJobHandler<P, R> {
  context(context: AbsurdJobHandler.Context)
  override suspend fun handle(params: P): R {
    context(RealJobHandlerContext(context)) {
      return delegate.handle(job = params)
    }
  }
}

internal class RealJobHandlerContext(
  private val delegate: AbsurdJobHandler.Context,
) : OsJobHandler.Context() {
  override val jobId: Uuid
    get() = delegate.taskId
  override val jobName: JobName<*, *>
    get() = delegate.taskName.toWasmo()

  override suspend fun <T> step(
    name: String,
    serializer: KSerializer<T>,
    block: suspend () -> T,
  ): T = delegate.step(name, serializer, block)

  override suspend fun <T> beginStep(
    name: String,
    serializer: KSerializer<T>,
  ) = RealStepHandle(delegate.beginStep(name, serializer))

  override suspend fun <T> awaitEvent(
    eventName: String,
    serializer: KSerializer<T>,
    stepName: String,
    timeout: Duration?,
  ): T = delegate.awaitEvent(eventName, serializer, stepName, timeout)

  override suspend fun sleepFor(stepName: String, duration: Duration) {
    delegate.sleepFor(stepName, duration)
  }

  override suspend fun sleepUntil(stepName: String, wakeAt: Instant) {
    delegate.sleepUntil(stepName, wakeAt)
  }
}

internal class RealStepHandle<T>(
  val delegate: AbsurdStepHandle<T>,
) : StepHandle<T> {
  override val name: String
    get() = delegate.name
  override val checkpointName: String
    get() = delegate.checkpointName
  override val done: Boolean
    get() = delegate.done
  override val result: T
    get() = delegate.result

  override suspend fun complete(result: T): T = delegate.complete(result)
}

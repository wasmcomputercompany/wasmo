package com.wasmo.postgresqloperator

import com.wasmo.common.logging.Logger
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.source

/**
 * Launch a Postgresql process in the same container and monitor it.
 */
@Inject
@SingleIn(OsScope::class)
internal class ExecPostgresqlOperator(
  private val logger: Logger,
  private val localPostgresql: LocalPostgresql,
) : PostgresqlOperator {
  private var processState = AtomicReference<ProcessState>(ProcessState.NotStarted)
  private val homelabFoundationPath = "/homelab-foundation".toPath()

  context(scope: CoroutineScope)
  override suspend fun await() {
    if (localPostgresql is LocalPostgresql.Exec) {
      execPostgresql(localPostgresql)
    }
  }

  context(scope: CoroutineScope)
  private fun execPostgresql(exec: LocalPostgresql.Exec) {
    val empty = try {
      FileSystem.SYSTEM.list("/wasmo/postgresql/18".toPath()).isEmpty()
    } catch (_: IOException) {
      true
    }

    if (empty) {
      gosuInitdb()
    }

    gosuPostgres(exec)
  }

  context(scope: CoroutineScope)
  private fun gosuInitdb() {
    logger.info("calling initdb...")
    val process = ProcessBuilder()
      .command(
        "/usr/bin/gosu",
        "postgres",
        "/usr/libexec/postgresql18/initdb",
        "--auth=trust",
        "--pgdata=/wasmo/postgresql/18",
        "--locale-provider=icu",
        "--locale=en_US",
      )
      .start()

    scope.coroutineContext[Job]!!.invokeOnCompletion {
      process.destroy()
    }

    collectStreams(process)

    val exitCode = process.waitFor()
    check(exitCode == 0) {
      "initdb failed: $exitCode"
    }

    logger.info("initdb success")
  }

  context(scope: CoroutineScope)
  private fun gosuPostgres(exec: LocalPostgresql.Exec) {
    logger.info("starting postgresql...")
    val process = ProcessBuilder()
      .command(
        "/usr/bin/gosu",
        "postgres",
        exec.postgres.toString(),
        "--config_file=${homelabFoundationPath / "postgresql.conf"}",
      )
      .start()

    scope.coroutineContext[Job]!!.invokeOnCompletion {
      process.destroy()
    }

    processState.set(ProcessState.Running(process))

    logger.info("postgresql started...")
    collectStreams(process)

    scope.launch {
      val exitCode = process.waitFor()

      val previous = processState.getAndSet(ProcessState.Stopped)
      if (previous !is ProcessState.Stopping) {
        error("postgresql exited unexpectedly, $exitCode")
      }
    }
  }

  context(scope: CoroutineScope)
  private fun collectStreams(process: Process) {
    scope.launch(Dispatchers.IO) {
      val source = process.inputStream.source().buffer()
      while (true) {
        val line = source.readUtf8Line() ?: break
        logger.info("postgresql: $line")
      }
    }

    scope.launch(Dispatchers.IO) {
      val source = process.errorStream.source().buffer()
      while (true) {
        val line = source.readUtf8Line() ?: break
        logger.info("postgresql: $line")
      }
    }
  }

  private sealed interface ProcessState {
    object NotStarted : ProcessState
    class Running(val process: Process) : ProcessState
    object Stopping : ProcessState
    object Stopped : ProcessState
  }
}

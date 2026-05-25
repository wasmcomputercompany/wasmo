package com.wasmo.postgresqloperator

import com.wasmo.identifiers.OsScope
import com.wasmo.sql.OsDatabaseInitializer
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
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
  private val localPostgresql: LocalPostgresql,
  private val initializer: OsDatabaseInitializer,
  private val commandOutputProcessorFactory: CommandOutputProcessor.Factory,
  private val postgresqlStderrProcessor: PostgresqlStderrProcessor,
) : PostgresqlOperator {
  context(scope: CoroutineScope)
  override suspend fun await() {
    if (localPostgresql is LocalPostgresql.Exec) {
      // For the suspend function's coroutineContext.
      withContext(Dispatchers.IO) {
        // For the `scope` context parameter.
        context(scope + Dispatchers.IO) {
          execPostgresql()
        }
      }
    }
  }

  context(scope: CoroutineScope)
  private suspend fun execPostgresql() {
    val empty = try {
      FileSystem.SYSTEM.list("/wasmo/postgresql/18".toPath()).isEmpty()
    } catch (_: IOException) {
      true
    }

    if (empty) {
      gosuInitdb()
    }

    scope.launch {
      gosuPostgres()
    }

    if (empty) {
      initializer.initialize()
    }
  }

  private suspend fun gosuInitdb() {
    coroutineScope {
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

      coroutineContext.job.invokeOnCompletion {
        process.destroy()
      }

      collectStream("initdb", Stream.Stdout, process)
      collectStream("initdb", Stream.Stderr, process)

      val exitCode = nameThread("initdb.join") {
        process.waitFor()
      }

      check(exitCode == 0 || coroutineContext.job.isCancelled) {
        "initdb failed: $exitCode"
      }
    }
  }

  private suspend fun gosuPostgres() {
    coroutineScope {
      val process = ProcessBuilder()
        .command(
          "/usr/bin/gosu",
          "postgres",
          "/usr/libexec/postgresql18/postgres",
          "--config_file=/homelab-foundation/postgresql.conf",
        )
        .start()

      coroutineContext.job.invokeOnCompletion {
        process.destroy()
      }

      collectStream("postgresql", Stream.Stdout, process)
      collectPostgresqlStderr(process)

      val exitCode = nameThread("postgresql.join") {
        process.waitFor()
      }

      check(coroutineContext.job.isCancelled) {
        "postgresql exited unexpectedly, $exitCode"
      }
    }
  }

  context(scope: CoroutineScope)
  private fun collectStream(name: String, stream: Stream, process: Process) {
    scope.launch {
      nameThread("$name.$stream") {
        val inputStream = when (stream) {
          Stream.Stdout -> process.inputStream
          Stream.Stderr -> process.errorStream
        }
        commandOutputProcessorFactory.create(name, stream)
          .processAll(inputStream.source().buffer())
      }
    }
  }

  context(scope: CoroutineScope)
  private fun collectPostgresqlStderr(process: Process) {
    scope.launch {
      nameThread("postgresql.Stderr") {
        postgresqlStderrProcessor.processAll(process.errorStream.source().buffer())
      }
    }
  }
}

package com.wasmo.postgresqloperator

import com.wasmo.common.logging.Logger
import com.wasmo.identifiers.OsScope
import com.wasmo.sql.OsDatabaseInitializer
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
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
  private val initializer: OsDatabaseInitializer,
) : PostgresqlOperator {
  context(scope: CoroutineScope)
  override suspend fun await() {
    if (localPostgresql is LocalPostgresql.Exec) {
      execPostgresql()
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

      coroutineContext.job.invokeOnCompletion {
        process.destroy()
      }

      collectStreams(process)

      val exitCode = process.waitFor()
      check(exitCode == 0 || coroutineContext.job.isCancelled) {
        "initdb failed: $exitCode"
      }

      logger.info("initdb success")
    }
  }

  private suspend fun gosuPostgres() {
    coroutineScope {
      logger.info("starting postgresql...")
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

      logger.info("postgresql started...")
      collectStreams(process)

      val exitCode = process.waitFor()

      check(coroutineContext.job.isCancelled) {
        "postgresql exited unexpectedly, $exitCode"
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
}

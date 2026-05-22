@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.wiring

import com.wasmo.db.Migrator
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobProcessor
import com.wasmo.ktor.ActionRouter
import com.wasmo.postgresqloperator.PostgresqlOperator
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.CoroutineScope
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@SingleIn(OsScope::class)
class WasmoService(
  private val postgresqlOperator: PostgresqlOperator,
  private val server: EmbeddedServer<*, *>,
  private val actionRouter: ActionRouter,
  private val jobProcessor: JobProcessor,
  private val migrator: Migrator,
  private val wasmoDb: SqlDatabase,
) {
  context(scope: CoroutineScope)
  suspend fun start() {
    postgresqlOperator.await()

    // AbsurdOsJobProcessor from jobProcessor.start() depends on this already having run.
    wasmoDb.transaction {
      migrator.ensureSchemaVersion()
    }
    actionRouter.createRoutes()
    jobProcessor.start()

    // don't permanently block the calling thread
    server.start(wait = false)
  }
}

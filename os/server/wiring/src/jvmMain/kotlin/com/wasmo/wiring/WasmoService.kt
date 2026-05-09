@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.wiring

import com.wasmo.db.Migrator
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobProcessor
import com.wasmo.ktor.ActionRouter
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.engine.EmbeddedServer
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@SingleIn(OsScope::class)
class WasmoService(
  private val server: EmbeddedServer<*, *>,
  private val actionRouter: ActionRouter,
  private val jobProcessor: JobProcessor,
) {
  suspend fun start() {
    actionRouter.createRoutes()
    jobProcessor.start()

    // don't permanently block the calling thread
    server.start(wait = false)
  }
}

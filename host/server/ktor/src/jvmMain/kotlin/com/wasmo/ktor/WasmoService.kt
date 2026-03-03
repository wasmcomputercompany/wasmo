@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.engine.EmbeddedServer

@Inject
@SingleIn(AppScope::class)
class WasmoService(
  private val server: EmbeddedServer<*, *>,
  private val actionRouter: ActionRouter,
) {
  fun start() {
    actionRouter.createRoutes()
    server.start(true)
  }
}


package com.wasmo.ktor

import com.wasmo.common.logging.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.log
import io.ktor.server.engine.EmbeddedServer

@Inject
@SingleIn(AppScope::class)
class KtorLogger(
  server: EmbeddedServer<*, *>,
) : Logger {
  private val log = server.application.log

  override fun info(message: String, throwable: Throwable?) {
    log.info(message, throwable)
  }
}

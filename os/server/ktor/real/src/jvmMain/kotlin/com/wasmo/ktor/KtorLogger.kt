package com.wasmo.ktor

import com.wasmo.common.logging.Logger
import com.wasmo.support.issues.Issue
import io.ktor.server.application.log
import io.ktor.server.engine.EmbeddedServer

class KtorLogger(
  server: EmbeddedServer<*, *>,
) : Logger {
  private val log = server.application.log

  override fun info(
    message: String,
    issues: List<Issue>,
  ) {
    log.info(
      buildString {
        append(message)
        for (issue in issues) {
          append("\n")
          append(issue)
        }
      },
    )
  }

  override fun info(message: String, throwable: Throwable?) {
    log.info(message, throwable)
  }
}

package com.wasmo.events

import com.wasmo.api.SqlExecuteStartedEvent
import com.wasmo.common.logging.Logger
import com.wasmo.identifiers.Event
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstallAppEvent
import com.wasmo.postgresqloperator.CliOutputEvent
import com.wasmo.postgresqloperator.PostgresqlLogEvent
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class LoggingEventListener(
  private val logger: Logger,
) : EventListener {
  override fun onEvent(event: Event) {
    when (event) {
      is SqlExecuteStartedEvent -> {
        logger.info(
          message ="executing: ${event.query} ${event.params.joinToString(", ")}"
        )
      }
      is InstallAppEvent -> {
        logger.info(
          message = "Installed on ${event.computerSlug.value}: ${event.appSlug.value}",
          issues = event.issues,
        )
      }

      is PostgresqlLogEvent -> {
        var message = event.message.replace(Regex("\\s+", RegexOption.DOT_MATCHES_ALL), " ")
        if (message.length > 256) {
          message = message.take(253) + "... (${message.length}-chars)"
        }

        logger.info(
          message = listOfNotNull(
            event.user,
            event.database,
            event.sqlStateErrorCode,
            event.severity,
            message,
          ).joinToString(separator = " "),
        )
      }

      is CliOutputEvent -> {
        logger.info("${event.name} ${event.stream} ${event.message}")
      }
    }
  }
}

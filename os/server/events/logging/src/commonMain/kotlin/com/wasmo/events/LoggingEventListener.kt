package com.wasmo.events

import com.wasmo.common.logging.Logger
import com.wasmo.identifiers.Event
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstallAppEvent
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class LoggingEventListener(
  private val logger: Logger,
) : EventListener {
  override fun onEvent(event: Event) {
    when (event) {
      is InstallAppEvent -> {
        logger.info(
          message = "Installed on ${event.computerSlug.value}: ${event.appSlug.value}",
          issues = event.issues,
        )
      }
    }
  }
}

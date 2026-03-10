package com.wasmo.events

import com.wasmo.common.logging.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class LoggingEventListener(
  private val logger: Logger,
) : EventListener {
  override fun onEvent(event: Event) {
    when (event) {
      is InstallAppEvent -> {
        logger.info(
          message = "Installed on ${event.computerSlug.value}: ${event.appSlug.value}",
          throwable = event.exception,
        )
      }
    }
  }
}

package com.wasmo.client.app

import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
) {
  fun start() {
    logger.info("hello")
  }
}

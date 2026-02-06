package app.rounds.client.app

import app.rounds.common.logging.ConsoleLogger
import app.rounds.common.logging.Logger

class WasmComputerApp(
  val logger: Logger = ConsoleLogger,
) {
  fun start() {
    logger.info("hello")
  }
}

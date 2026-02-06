package com.publicobject.wasmcomputer.client.app

import com.publicobject.wasmcomputer.common.logging.ConsoleLogger
import com.publicobject.wasmcomputer.common.logging.Logger

class WasmComputerApp(
  val logger: Logger = ConsoleLogger,
) {
  fun start() {
    logger.info("hello")
  }
}

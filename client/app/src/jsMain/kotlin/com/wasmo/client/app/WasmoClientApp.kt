package com.wasmo.client.app

import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  fun start() {
    logger.info("hello")

    renderComposableInBody {

      EnvironmentFrame(environment) { childStyle ->
        SignUpIntro(childStyle)
//        Home(childStyle)
      }
    }
  }
}

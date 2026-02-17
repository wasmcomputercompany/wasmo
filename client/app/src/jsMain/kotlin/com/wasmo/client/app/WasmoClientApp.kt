package com.wasmo.client.app

import com.wasmo.client.app.signup.SignUpChallengeCode
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  fun start() {
    renderComposableInBody {
      EnvironmentFrame(environment) { childStyle ->
        SignUpChallengeCode(childStyle) { _ ->
        }
      }
    }
  }
}

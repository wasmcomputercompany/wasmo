package com.wasmo.client.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.api.RealWasmoApi
import com.wasmo.client.app.signup.SignUpWorkflow
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  fun start() {
    val wasmoApi = RealWasmoApi()

    renderComposableInBody {
      var home by remember { mutableStateOf(true) }

      EnvironmentFrame(environment) { attrs ->
        if (home) {
          Home(
            attrs = attrs,
            showSignUp = environment.showSignUp,
          ) { event ->
            home = !home
          }
        } else {
          SignUpWorkflow(
            wasmoApi = wasmoApi,
            attrs = attrs,
          ) { event ->
            home = !home
          }
        }
      }
    }
  }
}

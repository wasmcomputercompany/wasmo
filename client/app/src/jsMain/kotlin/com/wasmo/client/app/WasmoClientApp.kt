package com.wasmo.client.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wasmo.api.RealWasmoApi
import com.wasmo.client.app.buildyours.BuildYoursScreen
import com.wasmo.client.app.buildyours.BuildYoursScreenEvent
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
      var showBuildForm by remember { mutableStateOf(false) }

      EnvironmentFrame(environment) { attrs ->
        if (home) {
          Home(
            attrs = attrs,
            showSignUp = environment.showSignUp,
          ) { event ->
            home = !home
          }
        } else {
          BuildYoursScreen(
            attrs = attrs,
            showBuildForm = showBuildForm,
            eventListener = {
              when (it) {
                BuildYoursScreenEvent.ClickBuildYours -> {
                  showBuildForm = true
                }

                BuildYoursScreenEvent.ClickCheckOut, BuildYoursScreenEvent.ClickQuestions -> {
                  showBuildForm = false
                  home = true
                }
              }
            },
          )
        }
      }
    }
  }
}

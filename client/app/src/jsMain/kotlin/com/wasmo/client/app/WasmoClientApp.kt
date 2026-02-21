package com.wasmo.client.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.RealWasmoApi
import com.wasmo.api.WasmoJson
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.client.app.buildyours.BuildYoursScreen
import com.wasmo.client.app.buildyours.BuildYoursScreenEvent
import com.wasmo.client.app.stripe.CheckoutScreen
import com.wasmo.client.app.stripe.CheckoutSession
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import com.wasmo.framework.PageData
import com.wasmo.framework.detectPageData
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  val pageData: PageData = detectPageData(WasmoJson)
  val stripePublishableKey = pageData.get<StripePublishableKey>("stripe_publishable_key")
    ?: error("required stripe_publishable_key pageData not found")
  val wasmoApi = RealWasmoApi()
  val checkoutSessionFactory = CheckoutSession.Factory(
    stripePublishableKey = stripePublishableKey,
    wasmoApi = wasmoApi,
  )

  fun start() {
    renderComposableInBody {
      val coroutineScope = rememberCoroutineScope()
      var home by remember { mutableStateOf(true) }
      var showBuildForm by remember { mutableStateOf(false) }
      var checkoutSessionState by remember { mutableStateOf<CheckoutSession?>(null) }

      EnvironmentFrame(environment) { attrs ->
        val checkoutSession = checkoutSessionState
        if (checkoutSession != null) {
          CheckoutScreen(checkoutSession)
          return@EnvironmentFrame
        }

        if (home) {
          Home(
            attrs = attrs,
            showSignUp = environment.showSignUp,
          ) { event ->
            home = !home
          }
          return@EnvironmentFrame
        }

        BuildYoursScreen(
          attrs = attrs,
          showBuildForm = showBuildForm,
          eventListener = {
            when (it) {
              BuildYoursScreenEvent.ClickBuildYours -> {
                showBuildForm = true
              }

              BuildYoursScreenEvent.ClickCheckOut -> {
                checkoutSessionState = checkoutSessionFactory.create(coroutineScope)
              }

              BuildYoursScreenEvent.ClickQuestions -> {
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

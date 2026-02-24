package com.wasmo.client.app.buildyours

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.routes.TeaserRoute
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.app.stripe.CheckoutScreen
import com.wasmo.client.app.stripe.CheckoutSession
import com.wasmo.client.framework.Ui
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class BuildYoursUi(
  val checkoutSessionFactory: CheckoutSession.Factory,
  val router: Router,
) : Ui {
  var showBuildForm by mutableStateOf(false)
  var checkoutSessionState by mutableStateOf<CheckoutSession?>(null)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val coroutineScope = rememberCoroutineScope()

    val checkoutSession = checkoutSessionState
    if (checkoutSession != null) {
      CheckoutScreen(checkoutSession)
      return
    }

    var formState by remember { mutableStateOf(FormState.Ready) }
    CompositionLocalProvider(LocalFormState provides formState) {
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
              router.goTo(TeaserRoute, TransitionDirection.POP)
            }
          }
        },
      )
    }
  }

  class Factory(
    val checkoutSessionFactory: CheckoutSession.Factory,
    val router: Router,
  ) {
    fun create() = BuildYoursUi(
      checkoutSessionFactory = checkoutSessionFactory,
      router = router,
    )
  }
}

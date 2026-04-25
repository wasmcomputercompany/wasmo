package com.wasmo.client.app.buildyours

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.routes.HomeRoute
import com.wasmo.client.app.FormState
import com.wasmo.client.app.LocalFormState
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.app.stripe.CheckoutScreen
import com.wasmo.client.app.stripe.CheckoutSession
import com.wasmo.client.framework.Ui
import com.wasmo.support.tokens.newToken
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class BuildYoursUi(
  private val checkoutSessionFactory: CheckoutSession.Factory,
  private val router: Router,
) : Ui {
  private val computerSpecToken = newToken()
  private var showBuildForm by mutableStateOf(false)
  private var checkoutSessionState by mutableStateOf<CheckoutSession?>(null)

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

            is BuildYoursScreenEvent.ClickCheckOut -> {
              checkoutSessionState = checkoutSessionFactory.create(
                coroutineScope,
                CreateComputerSpecRequest(
                  computerSpecToken = computerSpecToken,
                  slug = it.slug,
                ),
              )
            }

            BuildYoursScreenEvent.ClickQuestions -> {
              router.goTo(HomeRoute, TransitionDirection.POP)
            }
          }
        },
      )
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): BuildYoursUi
  }
}

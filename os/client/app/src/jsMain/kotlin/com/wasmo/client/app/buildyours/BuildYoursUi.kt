package com.wasmo.client.app.buildyours

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.WasmoApi
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.app.stripe.CheckoutScreen
import com.wasmo.client.app.stripe.CheckoutSession
import com.wasmo.client.framework.Ui
import com.wasmo.support.tokens.newToken
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class BuildYoursUi(
  private val coroutineScope: CoroutineScope,
  private val checkoutSessionFactory: CheckoutSession.Factory,
  private val router: Router,
  private val wasmoApi: WasmoApi,
) : Ui {
  private val computerSpecToken = newToken()
  private var checkoutSessionState by mutableStateOf<CheckoutSession?>(null)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val checkoutSession = checkoutSessionState
    if (checkoutSession != null) {
      CheckoutScreen(checkoutSession)
      return
    }

    BuildYoursScreen(
      attrs = attrs,
      eventListener = ::onEvent,
    )
  }

  fun onEvent(it: BuildYoursScreenEvent) {
    when (it) {
      is BuildYoursScreenEvent.ClickCreateComputer -> {
        val computerSlug = it.slug
        val createComputerSpecRequest = CreateComputerSpecRequest(
          computerSpecToken = computerSpecToken,
          slug = computerSlug,
        )
        coroutineScope.launch {
          val createComputerSpecResponse = wasmoApi.createComputerSpec(createComputerSpecRequest)
          when (createComputerSpecResponse) {
            CreateComputerSpecResponse.Success -> {
              router.goTo(ComputerHomeRoute(computerSlug), TransitionDirection.POP)
            }
            is CreateComputerSpecResponse.PaymentRequired -> {
              checkoutSessionState = checkoutSessionFactory.create(
                coroutineScope = coroutineScope,
                clientSecret =  createComputerSpecResponse.checkoutSessionClientSecret,
              )
            }
          }
        }
      }

      BuildYoursScreenEvent.ClickQuestions -> {
        router.goTo(HomeRoute, TransitionDirection.POP)
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): BuildYoursUi
  }
}

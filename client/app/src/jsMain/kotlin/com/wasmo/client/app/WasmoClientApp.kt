package com.wasmo.client.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.wasmo.api.RealWasmoApi
import com.wasmo.api.WasmoJson
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.client.app.browser.RealBrowser
import com.wasmo.client.app.buildyours.BuildYoursScreen
import com.wasmo.client.app.buildyours.BuildYoursScreenEvent
import com.wasmo.client.app.invite.InviteEvent
import com.wasmo.client.app.invite.InviteScreen
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.app.stripe.CheckoutScreen
import com.wasmo.client.app.stripe.CheckoutSession
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.AdminRoute
import com.wasmo.common.routes.AfterCheckoutRoute
import com.wasmo.common.routes.BuildYoursRoute
import com.wasmo.common.routes.ComputerHomeRoute
import com.wasmo.common.routes.ComputersRoute
import com.wasmo.common.routes.InviteRoute
import com.wasmo.common.routes.NotFoundRoute
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.common.routes.Route
import com.wasmo.common.routes.RoutingContext
import com.wasmo.common.routes.TeaserRoute
import com.wasmo.framework.PageData
import com.wasmo.framework.detectPageData
import kotlinx.coroutines.MainScope
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.HTMLElement

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  val scope = MainScope()
  val pageData: PageData = detectPageData(WasmoJson)
  val stripePublishableKey = pageData.get<StripePublishableKey>("stripe_publishable_key")
    ?: error("required stripe_publishable_key pageData not found")
  val routingContext = pageData.get<RoutingContext>("routing_context")
    ?: error("required routing_context pageData not found")
  val wasmoApi = RealWasmoApi()
  val checkoutSessionFactory = CheckoutSession.Factory(
    stripePublishableKey = stripePublishableKey,
    wasmoApi = wasmoApi,
  )
  val browser = RealBrowser()
  val routeCodec = RealRouteCodec(
    context = routingContext,
  )
  val router = Router(
    scope = scope,
    routeCodec = routeCodec,
    browser = browser,
  )

  fun start() {
    router.start()
    renderComposableInBody {
      val route = router.current.value ?: return@renderComposableInBody
      ShowRoute(route)
    }
  }

  @Composable
  fun ShowRoute(route: Route) {
    EnvironmentFrame(environment) { attrs ->
      when (route) {
        AdminRoute -> {
          H1 {
            Text("AdminRoute")
          }
        }

        is AfterCheckoutRoute -> {
          H1 {
            Text("AfterCheckoutRoute")
          }
        }

        BuildYoursRoute -> {
          BuildYoursRoute(
            attrs = attrs,
          )
        }

        is ComputerHomeRoute -> {
          H1 {
            Text("ComputerHomeRoute")
          }
        }

        ComputersRoute -> {
          H1 {
            Text("ComputersRoute")
          }
        }

        is InviteRoute -> {
          InviteRoute(attrs = attrs)
        }

        NotFoundRoute -> {
          H1 {
            Text("NotFoundRoute")
          }
        }

        TeaserRoute -> {
          TeaserRoute(attrs = attrs)
        }
      }
    }
  }

  @Composable
  fun TeaserRoute(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    TeaserScreen(
      attrs = attrs,
      showSignUp = environment.showSignUp,
    ) { event ->
      when (event) {
        HomeEvent.SignUp -> {
          router.goTo(BuildYoursRoute, TransitionDirection.PUSH)
        }
      }
    }
  }

  @Composable
  fun InviteRoute(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    InviteScreen(
      attrs = attrs,
    ) { event ->
      when (event) {
        InviteEvent.ClickAccept -> {
          router.goTo(BuildYoursRoute, TransitionDirection.REPLACE)
        }
      }
    }
  }

  @Composable
  fun BuildYoursRoute(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val coroutineScope = rememberCoroutineScope()
    var showBuildForm by remember { mutableStateOf(false) }
    var checkoutSessionState by remember { mutableStateOf<CheckoutSession?>(null) }

    val checkoutSession = checkoutSessionState
    if (checkoutSession != null) {
      CheckoutScreen(checkoutSession)
      return
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
            router.goTo(TeaserRoute, TransitionDirection.POP)
          }
        }
      },
    )
  }
}

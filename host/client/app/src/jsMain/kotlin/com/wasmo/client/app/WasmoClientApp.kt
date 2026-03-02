package com.wasmo.client.app

import androidx.compose.runtime.remember
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.RealWasmoApi
import com.wasmo.api.WasmoJson
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.client.app.browser.RealBrowser
import com.wasmo.client.app.buildyours.BuildYoursUi
import com.wasmo.client.app.computer.ComputerUi
import com.wasmo.client.app.computerlist.ComputerListUi
import com.wasmo.client.app.data.RealAccountDataService
import com.wasmo.client.app.invite.InviteUi
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.stripe.CheckoutSession
import com.wasmo.client.app.teaser.TeaserUi
import com.wasmo.client.app.ui.UiFactory
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.framework.PageData
import com.wasmo.framework.detectPageData
import com.wasmo.passkeys.RealPasskeyAuthenticator
import kotlinx.coroutines.MainScope
import org.jetbrains.compose.web.renderComposableInBody

class WasmoClientApp(
  val logger: Logger = ConsoleLogger,
  val environment: Environment,
) {
  private val scope = MainScope()
  private val pageData: PageData = detectPageData(WasmoJson)
  private val stripePublishableKey = pageData.get<StripePublishableKey>("stripe_publishable_key")
    ?: error("required stripe_publishable_key pageData not found")
  private val routingContext = pageData.get<RoutingContext>("routing_context")
    ?: error("required routing_context pageData not found")
  private val accountSnapshot = pageData.get<AccountSnapshot>("account_snapshot")
    ?: error("required account_snapshot pageData not found")
  private val wasmoApi = RealWasmoApi()
  private val checkoutSessionFactory = CheckoutSession.Factory(
    stripePublishableKey = stripePublishableKey,
    wasmoApi = wasmoApi,
  )
  private val browser = RealBrowser()
  private val routeCodec = RealRouteCodec(
    context = routingContext,
  )
  private val router = Router(
    scope = scope,
    routeCodec = routeCodec,
    browser = browser,
  )
  private val passkeyAuthenticator = RealPasskeyAuthenticator()

  private val accountDataService = RealAccountDataService(
    accountSnapshot = accountSnapshot,
  )
  private val uiFactory = UiFactory(
    pageData = pageData,
    inviteUiFactory = InviteUi.Factory(
      router = router,
      passkeyAuthenticator = passkeyAuthenticator,
      accountDataService = accountDataService,
      wasmoApi = wasmoApi,
      logger = logger,
      environment = environment,
    ),
    buildYoursUiFactory = BuildYoursUi.Factory(
      checkoutSessionFactory = checkoutSessionFactory,
      router = router,
    ),
    teaserUiFactory = TeaserUi.Factory(
      environment = environment,
      router = router,
    ),
    computerListUiFactory = ComputerListUi.Factory(
      router = router,
    ),
    computerUiFactory = ComputerUi.Factory(
      router = router,
    ),
  )

  fun start() {
    router.start()
    renderComposableInBody {
      val route = router.current.value ?: return@renderComposableInBody
      val ui = remember(route) { uiFactory.create(route) }

      EnvironmentFrame(environment) { attrs ->
        ui.Show(attrs = attrs)
      }
    }
  }
}

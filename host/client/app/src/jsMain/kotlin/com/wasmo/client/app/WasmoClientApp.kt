package com.wasmo.client.app

import androidx.compose.runtime.remember
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.WasmoApi
import com.wasmo.api.routes.RouteCodec
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
import com.wasmo.common.logging.Logger
import com.wasmo.framework.PageData
import com.wasmo.passkeys.RealPasskeyAuthenticator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.web.renderComposableInBody

@Inject
class WasmoClientApp(
  private val logger: Logger,
  private val environment: Environment,
  private val scope: CoroutineScope,
  private val pageData: PageData,
  private val wasmoApi: WasmoApi,
  private val accountSnapshot: AccountSnapshot,
  private val checkoutSessionFactory: CheckoutSession.Factory,
  private val browser: RealBrowser,
  private val routeCodec: RouteCodec,
) {
  private val computerSnapshot = pageData.get<ComputerSnapshot>("computer_snapshot")
  private val computerListSnapshot = pageData.get<ComputerListSnapshot>("computer_list_snapshot")
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
      routeCodec = routeCodec,
      router = router,
      computerListSnapshot = computerListSnapshot,
    ),
    computerUiFactory = ComputerUi.Factory(
      router = router,
      computerSnapshot = computerSnapshot,
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

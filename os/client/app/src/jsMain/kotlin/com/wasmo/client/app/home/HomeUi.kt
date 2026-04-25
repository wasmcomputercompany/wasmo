package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.SignUpRoute
import com.wasmo.api.routes.toURL
import com.wasmo.client.app.Environment
import com.wasmo.client.app.browser.Browser
import com.wasmo.client.app.computerlist.Item
import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class HomeUi(
  private val routeCodec: RouteCodec,
  private val environment: Environment,
  private val browser: Browser,
  private val router: Router,
  computerListSnapshot: ComputerListSnapshot?,
  private val accountDataService: AccountDataService,
) : Ui {
  private val computerListSnapshot: ComputerListSnapshot = computerListSnapshot
    ?: error("unexpected call of HomeUi.Factory.create(), snapshot is absent")
  private var menuVisible by mutableStateOf(false)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val accountSnapshot by accountDataService.accountSnapshotFlow.collectAsState()
    val menuModel = HomeMenuModel(
      visible = menuVisible,
      signedIn = accountSnapshot.emailAddresses.isNotEmpty(),
    )

    HomeScreen(
      attrs = attrs,
      menuModel = menuModel,
      eventListener = ::onHomeEvent,
      items = computerListSnapshot.items.map {
        Item(
          slug = it.slug,
          iframeSrc = routeCodec.encode(ComputerHomeRoute(it.slug)).toURL().href,
        )
      },
      teaser = computerListSnapshot.items.isEmpty(),
      showSignUp = environment.showSignUp,
    )
  }

  fun onHomeEvent(event: HomeEvent) {
    when (event) {
      HomeEvent.SignUp -> {
        router.goTo(BuildYoursRoute, TransitionDirection.PUSH)
      }

      HomeEvent.ClickScrim, HomeEvent.ClickDismissMenu -> {
        menuVisible = false
      }

      HomeEvent.ClickShowMenu -> {
        menuVisible = true
      }

      HomeEvent.ClickSignIn -> {
        menuVisible = false
        router.goTo(SignUpRoute, TransitionDirection.PUSH)
      }

      HomeEvent.ClickSignUp -> {
        menuVisible = false
        router.goTo(SignUpRoute, TransitionDirection.PUSH)
      }

      is HomeEvent.ClickComputer -> {
        router.goTo(
          ComputerHomeRoute(slug = event.slug),
          TransitionDirection.PUSH,
        )
      }

      HomeEvent.ClickSignOut -> {
        browser.locationHref = "/sign-out"
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): HomeUi
  }
}

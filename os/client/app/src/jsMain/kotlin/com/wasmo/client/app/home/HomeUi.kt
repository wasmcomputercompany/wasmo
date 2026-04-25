package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.SignUpRoute
import com.wasmo.api.routes.toURL
import com.wasmo.client.app.Environment
import com.wasmo.client.app.computerlist.Item
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class HomeUi(
  private val routeCodec: RouteCodec,
  private val environment: Environment,
  private val router: Router,
  computerListSnapshot: ComputerListSnapshot?,
) : Ui {
  private val computerListSnapshot: ComputerListSnapshot = computerListSnapshot
    ?: error("unexpected call of HomeUi.Factory.create(), snapshot is absent")

  private var menuModelFlow = MutableStateFlow(
    HomeMenuModel(
      visible = false,
      signedIn = true,
    ),
  )

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    val menuModel by menuModelFlow.collectAsState()
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
        menuModelFlow.update { it.copy(visible = false) }
      }

      HomeEvent.ClickShowMenu -> {
        menuModelFlow.update { it.copy(visible = true) }
      }

      HomeEvent.ClickSignIn -> {
        menuModelFlow.update { it.copy(visible = false) }
        router.goTo(SignUpRoute, TransitionDirection.PUSH)
      }

      HomeEvent.ClickSignUp -> {
        menuModelFlow.update { it.copy(visible = false) }
        router.goTo(SignUpRoute, TransitionDirection.PUSH)
      }

      is HomeEvent.ClickComputer -> {
        router.goTo(
          ComputerHomeRoute(slug = event.slug),
          TransitionDirection.PUSH,
        )
      }

      HomeEvent.ClickSignOut -> error("Unexpected event: $event")
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): HomeUi
  }
}

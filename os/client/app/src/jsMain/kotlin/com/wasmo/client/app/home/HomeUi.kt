package com.wasmo.client.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.SignUpRoute
import com.wasmo.client.app.Environment
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class HomeUi(
  private val environment: Environment,
  private val router: Router,
) : Ui {
  private var menuModel by mutableStateOf<HomeMenuModel?>(null)

  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    HomeScreen(
      attrs = attrs,
      showSignUp = environment.showSignUp,
      scrimVisible = menuModel != null,
      menuModel = menuModel,
      eventListener = ::onHomeEvent,
    )
  }

  fun onHomeEvent(event: HomeEvent) {
    when (event) {
      HomeEvent.SignUp -> {
        router.goTo(BuildYoursRoute, TransitionDirection.PUSH)
      }

      HomeEvent.ClickScrim -> {
        menuModel = null
      }

      HomeEvent.ClickShowMenu -> {
        menuModel = HomeMenuModel()
      }

      HomeEvent.ClickDismissMenu -> {
        menuModel = null
      }

      HomeEvent.ClickSignIn -> {
        menuModel = null
        router.goTo(SignUpRoute, TransitionDirection.PUSH)
      }

      HomeEvent.ClickSignUp -> {
        menuModel = null
        router.goTo(SignUpRoute, TransitionDirection.PUSH)
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): HomeUi
  }
}

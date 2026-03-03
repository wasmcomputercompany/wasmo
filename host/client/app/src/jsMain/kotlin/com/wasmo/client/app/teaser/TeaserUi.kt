package com.wasmo.client.app.teaser

import androidx.compose.runtime.Composable
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.client.app.Environment
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class TeaserUi(
  private val environment: Environment,
  private val router: Router,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    TeaserScreen(
      attrs = attrs,
      showSignUp = environment.showSignUp,
    ) { event ->
      when (event) {
        TeaserEvent.SignUp -> {
          router.goTo(BuildYoursRoute, TransitionDirection.PUSH)
        }
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(): TeaserUi
  }
}

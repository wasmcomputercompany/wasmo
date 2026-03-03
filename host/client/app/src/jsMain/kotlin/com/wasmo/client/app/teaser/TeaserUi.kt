package com.wasmo.client.app.teaser

import androidx.compose.runtime.Composable
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.client.app.Environment
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class TeaserUi(
  val environment: Environment,
  val router: Router,
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

  @Inject
  @SingleIn(AppScope::class)
  class Factory(
    val environment: Environment,
    val router: Router,
  ) {
    fun create() = TeaserUi(
      environment = environment,
      router = router,
    )
  }
}

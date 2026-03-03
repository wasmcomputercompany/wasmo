package com.wasmo.client.app

import androidx.compose.runtime.remember
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.ui.UiFactory
import dev.zacsweers.metro.Inject
import org.jetbrains.compose.web.renderComposableInBody

@Inject
class WasmoClientApp(
  private val environment: Environment,
  private val router: Router,
  private val uiFactory: UiFactory,
) {
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

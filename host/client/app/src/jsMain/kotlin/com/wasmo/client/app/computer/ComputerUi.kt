package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.launcher.Icon
import com.wasmo.launcher.LauncherIconList
import com.wasmo.launcher.LauncherScreen
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

class ComputerUi(
  val slug: ComputerSlug,
  val router: Router,
  val computerSnapshot: ComputerSnapshot,
) : Ui {
  @Composable
  override fun Show(
    attrs: AttrsScope<HTMLElement>.() -> Unit,
  ) {
    LauncherScreen {
      LauncherIconList {
        for (app in computerSnapshot.apps) {
          Icon(app.label, app.maskableIconUrl) {
            router.goTo(AppRoute(slug, app.slug), TransitionDirection.PUSH)
          }
        }
      }
    }
  }

  class Factory(
    val router: Router,
    val computerSnapshot: ComputerSnapshot?,
  ) {
    fun create(route: ComputerHomeRoute) = ComputerUi(
      slug = route.slug,
      router = router,
      computerSnapshot = computerSnapshot
        ?: error("unexpected call of ComputerUiFactory.create(), computer snapshot is absent"),
    )
  }
}

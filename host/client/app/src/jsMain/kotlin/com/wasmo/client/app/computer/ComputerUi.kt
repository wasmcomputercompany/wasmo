package com.wasmo.client.app.computer

import androidx.compose.runtime.Composable
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.routes.AppRoute
import com.wasmo.client.app.routing.Router
import com.wasmo.client.app.routing.TransitionDirection
import com.wasmo.client.framework.Ui
import com.wasmo.launcher.Icon
import com.wasmo.launcher.LauncherIconList
import com.wasmo.launcher.LauncherScreen
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

@AssistedInject
class ComputerUi(
  @Assisted private val slug: ComputerSlug,
  private val router: Router,
  computerSnapshot: ComputerSnapshot?,
) : Ui {

  private val computerSnapshot: ComputerSnapshot = computerSnapshot
    ?: error("unexpected call of ComputerUi.Factory.create(), snapshot is absent")

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

  @AssistedFactory
  interface Factory {
    fun create(slug: ComputerSlug): ComputerUi
  }
}
